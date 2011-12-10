package editor;

public class WurstController {
	private MainView mainView;
	private FileController fileController;

	public WurstController(MainView mainView) {
		this.mainView = mainView;
		this.fileController = new FileController(mainView);
	}

	public FileController getFileController() {
		return fileController;
	}
}
