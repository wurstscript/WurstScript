package de.peeeq.eclipsewurstplugin.builder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.gui.WurstGui;

public class WurstGuiEclipse extends WurstGui {

	private IProgressMonitor monitor;
	private String taskName;
	
	public WurstGuiEclipse(IProgressMonitor monitor) {
		this.monitor = monitor;
		this.taskName = "Compiling wurst";
		monitor.beginTask(taskName, 100);
	}
	

	@Override
	public void sendProgress(String whatsRunningNow) {
		if (whatsRunningNow != null) {
			WLogger.info("progress: " + whatsRunningNow);
		}
		monitor.worked(1);
		monitor.subTask(whatsRunningNow);
	}

	@Override
	public void sendFinished() {
		monitor.done();
	}
	

	@Override
	public void showInfoMessage(final String message) {
		final Display display = Display.getDefault();
		display.syncExec(new Runnable() {
			
			@Override
			public void run() {
				Shell shell = display.getActiveShell();
				MessageBox dialog = 
						  new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK);
						dialog.setText("Wurst Info");
						dialog.setMessage(message);
						dialog.open(); 
			}
		});
	}
}
