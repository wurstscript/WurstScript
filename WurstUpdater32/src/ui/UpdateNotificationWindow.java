package ui;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;

import control.UpdaterController;

public class UpdateNotificationWindow {

	protected Shell shlWurstNote;
	public Label lblThereAreNew;
	public Button btnStartUpdater;



	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlWurstNote.open();
		shlWurstNote.layout();
		Monitor primary = display.getPrimaryMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shlWurstNote.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shlWurstNote.setLocation (x, y);
		while (!shlWurstNote.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlWurstNote = new Shell();
		shlWurstNote.setSize(300, 108);
		shlWurstNote.setText("Wurst Note");
		
		lblThereAreNew = new Label(shlWurstNote, SWT.NONE);
		lblThereAreNew.setAlignment(SWT.CENTER);
		lblThereAreNew.setBounds(10, 10, 264, 15);
		lblThereAreNew.setText("There are new updates available!");
		
		btnStartUpdater = new Button(shlWurstNote, SWT.NONE);
		btnStartUpdater.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent arg0) {
				shlWurstNote.dispose();
				new UpdaterController();
				
			}
		});
		btnStartUpdater.setBounds(85, 31, 110, 25);
		btnStartUpdater.setText("Start Updater");

	}

}
