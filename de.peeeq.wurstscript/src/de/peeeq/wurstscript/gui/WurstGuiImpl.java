package de.peeeq.wurstscript.gui;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.Utils;

public class WurstGuiImpl implements WurstGui {


	private List<CompileError> errors = Lists.newLinkedList(); // this is not concurrent, because we only use this list from the main thread

	private volatile Queue<CompileError> errorQueue = new ConcurrentLinkedQueue<CompileError>();
	private volatile double progress = 0.0;
	private volatile boolean finished = false;
	private volatile String currentlyWorkingOn = "";
	private GuiUpdater guiUpdater;

	public WurstGuiImpl() {
		// this constructor is called from the main thread, so we should not create the gui
		// here. This would block the main compiler thread until the gui is created.
		guiUpdater = new GuiUpdater();
		guiUpdater.start();
	}

	/**
	 * this is a thread which creates and updates the status window and the error window
	 * 
	 * this is all done asynchronously, so the main compiler thread is not blocked
	 */
	class GuiUpdater extends Thread {
		private WurstStatusWindow statusWindow = null;
		private WurstErrorWindow errorWindow = null;


		public GuiUpdater() {
		}



		@Override
		public void run() {
			try {
				
				// init the windows:
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						statusWindow = new WurstStatusWindow();
						errorWindow = new WurstErrorWindow();
					}
				});
				
				
				// main loop: wait until finished and send the errors in the queue to the actual gui
				while (!finished || !errorQueue.isEmpty()) {

					// Update the UI:
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							for (CompileError elem = errorQueue.poll() ;elem != null; elem = errorQueue.poll()) {
								errorWindow.sendError(elem);
							}
							statusWindow.sendProgress(currentlyWorkingOn, progress);
						}
					});
					
					Utils.sleep(300);
				}
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						errorWindow.sendFinished();
						statusWindow.sendFinished();
					}
				});
			} catch (Throwable e) {
				WLogger.severe(e);
				throw new Error(e);
			}
		}

	}


	@Override
	public void sendError(CompileError err) {
		errorQueue.add(err);
		errors.add(err);
	}


	@Override
	public void sendProgress(String whatsRunningNow, double percent) {
		progress = percent;
		this.currentlyWorkingOn = whatsRunningNow;
	}


	@Override
	public void sendFinished() {
		finished = true;
	}


	@Override
	public int getErrorCount() {
		return errors.size();
	}


	@Override
	public String getErrors() {
		return Utils.join(errors, "\n");
	}


	@Override
	public List<CompileError> getErrorList() {
		return Lists.newLinkedList(errors);
	}


}
