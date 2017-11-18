package workers;

import file.WurstProjectConfig;

import javax.swing.*;

public class ProjectCreateWorker extends SwingWorker<Boolean, Void> {
    private final WurstProjectConfig config;

    public ProjectCreateWorker(WurstProjectConfig config) {
        this.config = config;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        WurstProjectConfig.handleCreate(config);
        return null;
    }
}
