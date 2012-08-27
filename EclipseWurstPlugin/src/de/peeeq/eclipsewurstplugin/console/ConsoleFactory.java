package de.peeeq.eclipsewurstplugin.console;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleFactory;
import org.eclipse.ui.console.IConsoleView;

public class ConsoleFactory implements IConsoleFactory {

	private WurstConsole console = null;
	private int counter = 1;
	
	@Override
	public void openConsole() {
		 IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	        if (window != null) {
	            IWorkbenchPage page = window.getActivePage();
	            if (page != null) {
	                try {
	                    String secondaryId = "Console View #" + counter ; //$NON-NLS-1$
	                    IConsoleView cv = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW, secondaryId, 1);
	                    counter++;
	                } catch (PartInitException e) {
	                    ConsolePlugin.log(e);
	                }
	            }
	        }

	}

}
