package de.peeeq.eclipsewurstplugin.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.locks.Lock;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

import de.peeeq.eclipsewurstplugin.editor.WurstEditor;
import de.peeeq.wurstscript.WLogger;

public class WurstConsole extends IOConsole implements Runnable {

	
	public static final String NAME = "Wurst Console";
	private WurstREPL repl;

	public WurstConsole() {
		super(NAME, null);
		repl = new WurstREPL(null, newOutputStream());
	
		new Thread(this).start();
	}
	
	public void run() {
		final BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream()));
		final IOConsoleOutputStream out = newOutputStream();
		try {
			out.write("Welcome to Wurst, the best language since sliced bread!\n");
			out.write("Type 'help' to get help.\n");
		} catch (IOException e1) {
			WLogger.severe(e1);
		}
		
		try {
			while (!out.isClosed()) {			
				out.write("> ");
				final String line = in.readLine();
				if (line == null) {
					break;
				}
				
				attachConsoleToCurrentEditor(out);
				repl.putLine(line);
			}
		} catch (IOException e) {
			if (e.getMessage().contains("Stream Closed")) {
				// ignore
			} else {
				WLogger.severe(e);
			}
		} catch (InterruptedException e) {
			WLogger.severe(e);
		}
	}

	public void attachConsoleToCurrentEditor(
			final IOConsoleOutputStream out) throws InterruptedException {
		final boolean[] locked = new boolean[]{true};
		
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				WurstEditor editor = WurstEditor.getActiveEditor();
				if (editor == null) {
					try {
						out.write("No wurst editor is active.\n");
					} catch (IOException e) {
					}
				} else {
					repl.setModelManager(editor.getModelManager());
					repl.setEditorCompilationUnit(editor.getCompilationUnit());
				}
				synchronized (locked) {
					locked[0] = false;
					locked.notifyAll();
				}
			}
		});
		
		while (locked[0]) {
			synchronized (locked) {
				locked.wait();
			}
		}
	}
}
