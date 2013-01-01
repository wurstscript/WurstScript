package ui;

import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;

import control.UpdaterController;

public class UpdaterWindow {

	public Shell shlWurstUpdater;
	public Label lblNoUpdatesAvailable;
	public Button btnUpdate;
	public ProgressBar progressBar;
	public Label lblStatus;
	
	UpdaterController ucontrol;

	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open(final UpdaterController uct) {
		ucontrol = uct;
		Display display = Display.getDefault();
		createContents();
		shlWurstUpdater.open();
		shlWurstUpdater.layout();
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shlWurstUpdater.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shlWurstUpdater.setLocation (x, y);
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				uct.showInfo();
				
			}
		
        });
		while (!shlWurstUpdater.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlWurstUpdater = new Shell();
		shlWurstUpdater.setSize(400, 153);
		shlWurstUpdater.setText("Wurst Updater");
		
		lblNoUpdatesAvailable = new Label(shlWurstUpdater, SWT.NONE);
		lblNoUpdatesAvailable.setAlignment(SWT.CENTER);
		lblNoUpdatesAvailable.setBounds(10, 10, 364, 15);
		lblNoUpdatesAvailable.setText("No Updates available");
		
		btnUpdate = new Button(shlWurstUpdater, SWT.NONE);
		btnUpdate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				ResponseDialog rs = new ResponseDialog(shlWurstUpdater, 0);
				rs.open();
				btnUpdate.setEnabled(false);
				progressBar.setSelection(10);
				lblStatus.setText("Downloading");
				try {
					if (ucontrol.compiler) {
						ucontrol.updateCompiler();
					}else{
						ucontrol.updatePack();
					}
				}catch(Exception e1) {
					e1.printStackTrace();
				}
					
			}
		});
		btnUpdate.setEnabled(false);
		btnUpdate.setBounds(150, 31, 75, 25);
		btnUpdate.setText("Update");
		
		progressBar = new ProgressBar(shlWurstUpdater, SWT.NONE);
		progressBar.setBounds(10, 62, 364, 17);
		
		lblStatus = new Label(shlWurstUpdater, SWT.NONE);
		lblStatus.setBounds(10, 85, 102, 15);
		lblStatus.setText("Idle");

	}
}
