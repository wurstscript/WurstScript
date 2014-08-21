package de.peeeq.wurstio.gui;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Preconditions;

import de.peeeq.wurstio.UtilsIO;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.attributes.CompileError.ErrorType;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.Utils;

public class WurstGuiImpl extends WurstGui {


	private volatile Queue<CompileError> errorQueue = new ConcurrentLinkedQueue<CompileError>();
	private volatile double progress = 0.0;
	private volatile boolean finished = false;
	private volatile @Nullable String currentlyWorkingOn = "";
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
		private @Nullable WurstStatusWindow statusWindow = null;
		private @Nullable WurstErrorWindow errorWindow = null;


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
				
				WurstStatusWindow statusWindow = this.statusWindow;
				WurstErrorWindow errorWindow = this.errorWindow;
				Preconditions.checkNotNull(statusWindow);
				Preconditions.checkNotNull(errorWindow);
				
				// main loop: wait until finished and send the errors in the queue to the actual gui
				while (!finished || !errorQueue.isEmpty()) {

					// Update the UI:
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							for (CompileError elem = pollErrorQueue(); elem != null; elem = pollErrorQueue()) {
								errorWindow.sendError(elem);
							}
							statusWindow.sendProgress(currentlyWorkingOn, progress);
						}

						private @Nullable CompileError pollErrorQueue() {
							return errorQueue.poll();
						}
					});
					
					UtilsIO.sleep(300);
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
		super.sendError(err);
		if (err.getErrorType() == ErrorType.ERROR) {
			errorQueue.add(err);
		}
	}

	boolean show = true;

	@Override
	public void sendProgress(@Nullable String whatsRunningNow, double percent) {
		if (whatsRunningNow != null) {
			WLogger.info("progress: " + whatsRunningNow);
		}
		if (percent >= progress) {
			progress = percent;
		} else {
			if (show) {
				WLogger.info("Progress bar going backwards: \n " +
						"changed from " + progress + " to " + percent + "\n" + whatsRunningNow + "\n " + Utils.printStackTrace(Thread.currentThread().getStackTrace()));
				show = false;
			}
		}
		this.currentlyWorkingOn = whatsRunningNow;
	}


	@Override
	public void sendFinished() {
		finished = true;
	}


	@Override
	public void showInfoMessage(final String message) {
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message);
				}
			});
		} catch (Exception e) {
			throw new Error(e);
		}
	}

}
