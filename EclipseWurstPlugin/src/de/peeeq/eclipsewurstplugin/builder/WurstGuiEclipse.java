package de.peeeq.eclipsewurstplugin.builder;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.google.common.collect.Lists;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.attributes.CompileError;
import de.peeeq.wurstscript.gui.WurstGui;
import de.peeeq.wurstscript.utils.NotNullList;

public class WurstGuiEclipse implements WurstGui {

	private List<CompileError> errors = new NotNullList<CompileError>();
	private IProgressMonitor monitor;
	private String taskName;
	private int lastPercent = 0;
	
	public WurstGuiEclipse(IProgressMonitor monitor) {
		this.monitor = monitor;
		this.taskName = "Compiling wurst";
		monitor.beginTask(taskName, 100);
	}
	
	@Override
	public void sendError(CompileError err) {
		errors.add(err);
	}

	@Override
	public void sendProgress(String whatsRunningNow, double percent) {
		if (whatsRunningNow != null) {
			WLogger.info("progress: " + whatsRunningNow);
		}
		int p = (int) (10000*percent);
		int workDone = p - lastPercent;
		if (workDone <= 0) {
			workDone = 1;
		}
		monitor.worked(workDone);
		lastPercent = p;
		monitor.subTask(whatsRunningNow);
	}

	@Override
	public void sendFinished() {
		monitor.done();
	}
	
	public String getErrors() {
		String result = "";
		for (CompileError e : errors) {
			result += e + "\n";
		}
		return result;
	}

	public int getErrorCount() {
		return errors.size();
	}
	
	@Override
	public List<CompileError> getErrorList() {
		return Lists.newArrayList(errors);
	}

	@Override
	public void clearErrors() {
		errors.clear();
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
