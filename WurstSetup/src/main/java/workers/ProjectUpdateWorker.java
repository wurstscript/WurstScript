package workers;

import file.WurstProjectConfig;

import javax.swing.*;

public class ProjectUpdateWorker extends SwingWorker<Boolean, Void> {
    private final WurstProjectConfig config;

    public ProjectUpdateWorker(WurstProjectConfig config) {
        this.config = config;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        WurstProjectConfig.handleUpdate(config);
        return null;
    }
}
