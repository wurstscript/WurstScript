package wursteditor.controller;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.PatternFilenameFilter;

import de.peeeq.wurstscript.attributes.CompileError;

import wursteditor.WurstEditFileView;
import wursteditor.WurstEditorView;

/**
 * this class connects the gui with the underlying logic 
 */
public class WurstEditorController {
	private WurstEditorView view;

	private static WurstEditorController instance = null;
	
	public static WurstEditorController getInstance() {
		return instance;
	}
	
	public WurstEditorController(final WurstEditorView v) {
		instance = this;
		v.getOpenProjectButton().addActionListener(onClick_openProject());
		v.getSaveFileButton().addActionListener(onClick_saveFile());
		v.getUndoButton().addActionListener(onclick_undo(v));
		v.getRedoButton().addActionListener(onclick_redo(v));
	}

	
	private ActionListener onclick_redo(final WurstEditorView v) {
		this.view = v;
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				v.getSyntaxCodeArea().redoLastAction();
			}
		};
	}

	private ActionListener onclick_undo(final WurstEditorView v) {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				v.getSyntaxCodeArea().undoLastAction();
			}
		};
	}

	private ActionListener onClick_openProject() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog chooser = new FileDialog(view.getFrame());
				FilenameFilter filter = new PatternFilenameFilter(".*\\.wurst|.*\\.wurstproject");
				chooser.setFilenameFilter(filter);
				chooser.setVisible(true);
				File file = new File(chooser.getDirectory() + chooser.getFile());
				openFile(file);
			}

			
		};
	}
	
	private void openFile(File file) {
		if (!file.exists()) {
			throw new Error("File " +file.getAbsolutePath() + " does not exist.");
		}
		// check if already opened:
		for (int i = 0; i < view.getjTabbedPane2().getTabCount(); i++) {
			if (view.getjTabbedPane2().getTabComponentAt(i) instanceof WurstEditFileView) {
				WurstEditFileView we = (WurstEditFileView) view.getjTabbedPane2().getTabComponentAt(i);
				if (we.getFileName().equals(file.getAbsolutePath())) {
					view.getjTabbedPane2().setSelectedIndex(i);
					return;
				}
			}
		}
		
		
		WurstEditFileView fileView = new WurstEditFileView(file.getAbsolutePath());
		String text = "";
		try {
			text = Files.toString(file, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		fileView.getSyntaxCodeArea().setText(text);
		view.getjTabbedPane2().addTab(file.getName(), fileView);
		view.getjTabbedPane2().setSelectedComponent(fileView);
	}
	
	private ActionListener onClick_saveFile() {
		return new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Component current = view.getjTabbedPane2().getSelectedComponent();
				if (current instanceof WurstEditFileView) {
					WurstEditFileView we = (WurstEditFileView) current;
					String text = we.getSyntaxCodeArea().getText();
					String fileName = we.getFileName();
					
					try {
						Files.write(text, new File(fileName), Charsets.UTF_8);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.err.println("no file selected");
				}
			}
		};
	}

	@SuppressWarnings("unchecked")
	public void setErrors(List<CompileError> errorList) {
		view.getjList1().setListData(errorList.toArray());
	}

}
