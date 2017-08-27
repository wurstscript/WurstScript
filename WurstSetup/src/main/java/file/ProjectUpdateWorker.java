package file;

import javax.swing.*;
import java.io.File;

public class ProjectUpdateWorker extends SwingWorker<Boolean, Void> {
    private final File buildFile;

    public ProjectUpdateWorker(File buildFile) {
        this.buildFile = buildFile;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        WurstProjectConfig.handleUpdate(buildFile);
        return null;
    }
}
