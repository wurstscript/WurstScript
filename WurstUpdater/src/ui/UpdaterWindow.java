package ui;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

import control.NetControl;
import control.UpdaterController;
import control.UpdaterInfos;

public class UpdaterWindow {

	protected Shell shell;
	public TextViewer textViewer;
	public Button btnUpdatePack;
	public Button btnUpdateCompiler;
	public Label lblCurrentPack;
	public Label lblCurrentCompiler;
	public Label lblLatestCompiler;
	public Button btnClose;
	public Label lblLatestPack;
	public Label currentPackVersion;
	public Label currentCompilerVersion;
	public Label latestPackVersion;
	public Label latestCompilerVersion;
	public Label lblStatus;
	public Display display;
	public UpdaterController ucontrol;
	
	/**
	 * Open the window.
	 */
	public void open(final UpdaterController uct) {
		display = Display.getDefault();
		createContents();
		shell.open();
		shell.layout();
		ucontrol = uct;
		/* Create and display the form */
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				uct.showInfo();
				
			}
		
        });
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(450, 270);
		shell.setText("SWT Application");
		
		textViewer = new TextViewer(shell, SWT.BORDER);
		StyledText styledText = textViewer.getTextWidget();
		styledText.setText("History:");
		styledText.setEditable(false);
		styledText.setBounds(10, 10, 414, 137);
		
		btnUpdatePack = new Button(shell, SWT.NONE);
		btnUpdatePack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					ucontrol.updatePack();
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnUpdatePack.setEnabled(false);
		btnUpdatePack.setBounds(127, 195, 98, 25);
		btnUpdatePack.setText("Update Pack");
		
		btnUpdateCompiler = new Button(shell, SWT.NONE);
		btnUpdateCompiler.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				try {
					ucontrol.updateCompiler();
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnUpdateCompiler.setEnabled(false);
		btnUpdateCompiler.setBounds(231, 195, 112, 25);
		btnUpdateCompiler.setText("Update Compiler");
		
		lblCurrentPack = new Label(shell, SWT.NONE);
		lblCurrentPack.setBounds(10, 153, 106, 15);
		lblCurrentPack.setText("Current Pack:");
		
		lblCurrentCompiler = new Label(shell, SWT.NONE);
		lblCurrentCompiler.setBounds(10, 174, 106, 15);
		lblCurrentCompiler.setText("Current Compiler:");
		
		lblLatestCompiler = new Label(shell, SWT.NONE);
		lblLatestCompiler.setBounds(208, 174, 98, 15);
		lblLatestCompiler.setText("Latest Compiler:");
		
		btnClose = new Button(shell, SWT.NONE);
		btnClose.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				shell.dispose();
			}
		});
		btnClose.setBounds(349, 195, 75, 25);
		btnClose.setText("Close");
		
		lblLatestPack = new Label(shell, SWT.NONE);
		lblLatestPack.setBounds(208, 153, 98, 15);
		lblLatestPack.setText("Latest Pack:");
		
		currentPackVersion = new Label(shell, SWT.NONE);
		currentPackVersion.setBounds(127, 153, 75, 15);
		
		currentCompilerVersion = new Label(shell, SWT.NONE);
		currentCompilerVersion.setBounds(127, 174, 75, 15);
		
		latestPackVersion = new Label(shell, SWT.NONE);
		latestPackVersion.setBounds(312, 153, 89, 15);
		
		latestCompilerVersion = new Label(shell, SWT.NONE);
		latestCompilerVersion.setBounds(312, 174, 89, 15);
		
		lblStatus = new Label(shell, SWT.NONE);
		lblStatus.setBounds(10, 205, 111, 15);
		lblStatus.setText("Status:");

	}
}
