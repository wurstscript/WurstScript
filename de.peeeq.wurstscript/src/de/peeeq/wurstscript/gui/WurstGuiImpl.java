package de.peeeq.wurstscript.gui;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.google.common.collect.Lists;
import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.utils.Utils;

public class WurstGuiImpl implements WurstGui {

	private volatile Queue<CompileError> errorQueue = new ConcurrentLinkedQueue<CompileError>();
	private List<CompileError> errors = Lists.newLinkedList(); // this is not concurrent, because we only use this list from the main thread
	private volatile double progress = 0.0;
	private volatile boolean finished = false;
	private volatile String currentlyWorkingOn = "";
	private TheGui guiAccsess;

	public WurstGuiImpl() {
		WurstStatusWindow wstatus = new WurstStatusWindow();
		WurstErrorWindow werror = new WurstErrorWindow();
		wstatus.errorWindow = werror;
		try {
			werror.ab = new About(werror,true);
			werror.ab.setVisible(false);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		guiAccsess = new TheGui(wstatus);
		wstatus.mainGui = guiAccsess;
		new Thread(guiAccsess).start();
	}

	
	class TheGui implements Runnable {
		public WurstGui gui;
		
		public TheGui(WurstGui ui) {
			gui = ui;			
		}	
		
		@Override
		public void run() {
			while (!finished || !errorQueue.isEmpty()) {
				Utils.sleep(300);

				try {
					// Update the UI:
					SwingUtilities.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							for (CompileError elem = errorQueue.poll() ;elem != null; elem = errorQueue.poll()) {
								gui.sendError(elem);
							}
							gui.sendProgress(currentlyWorkingOn, progress);
						}
					});
				} catch (Exception e) {
					throw new Error(e);
				}
			}
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						gui.sendFinished();
					}
				});
			} catch (Exception e) {
				WLogger.severe(e.toString());
				throw new Error(e);
			}
		}


	}


	@Override
	public void sendError(CompileError err) {
		guiAccsess.gui.sendError(err);
		
	}


	@Override
	public void sendProgress(String whatsRunningNow, double percent) {
		guiAccsess.gui.sendProgress(whatsRunningNow, percent);
		
	}


	@Override
	public void sendFinished() {		
		finished = true;
	}


	@Override
	public int getErrorCount() {
		return guiAccsess.gui.getErrorCount();
	}


	@Override
	public String getErrors() {
		return guiAccsess.gui.getErrors();
	}


	@Override
	public List<CompileError> getErrorList() {
		return guiAccsess.gui.getErrorList();
	}


}
