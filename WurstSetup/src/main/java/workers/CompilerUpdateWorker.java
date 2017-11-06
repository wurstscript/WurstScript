package workers;

import file.GlobalWurstConfig;
import ui.Init;

import javax.swing.*;

public class CompilerUpdateWorker extends SwingWorker<Boolean, Void> {

    public CompilerUpdateWorker() {
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        GlobalWurstConfig.handleUpdate();
        Init.refreshUi();
        return null;
    }
}
