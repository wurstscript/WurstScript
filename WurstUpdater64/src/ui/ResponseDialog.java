package ui;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;

public class ResponseDialog extends Dialog {

	protected Object result;
	protected Shell shlInfo;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ResponseDialog(Shell parent, int style) {
		super(parent, style);
		setText("Note");
		Monitor primary = parent.getMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = parent.getBounds ();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shlInfo.setLocation (x, y);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlInfo.open();
		shlInfo.layout();
		Display display = getParent().getDisplay();
		while (!shlInfo.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlInfo = new Shell(getParent(), getStyle());
		shlInfo.setSize(290, 117);
		shlInfo.setText("Info");
		
		Label lblTheUpdaterMight = new Label(shlInfo, SWT.NONE);
		lblTheUpdaterMight.setAlignment(SWT.CENTER);
		lblTheUpdaterMight.setBounds(10, 10, 264, 15);
		lblTheUpdaterMight.setText("The updater might stop responding.");
		
		Label lblWaitForThe = new Label(shlInfo, SWT.NONE);
		lblWaitForThe.setAlignment(SWT.CENTER);
		lblWaitForThe.setBounds(10, 31, 264, 15);
		lblWaitForThe.setText("Wait for the updating to finish!");
		
		Button btnNewButton = new Button(shlInfo, SWT.NONE);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				shlInfo.dispose();
			}
		});
		btnNewButton.setBounds(100, 52, 75, 25);
		btnNewButton.setText("OK");

	}
}
