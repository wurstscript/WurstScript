/*
 * WurstEditorApp.java
 */

package wursteditor;

import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

import wursteditor.controller.WurstEditorController;

/**
 * The main class of the application.
 */
public class WurstEditorApp extends SingleFrameApplication {

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        WurstEditorView wurstEditorView = new WurstEditorView(this);
        new WurstEditorController(wurstEditorView);
		show(wurstEditorView);
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of WurstEditorApp
     */
    public static WurstEditorApp getApplication() {
        return Application.getInstance(WurstEditorApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        launch(WurstEditorApp.class, args);
    }
}
