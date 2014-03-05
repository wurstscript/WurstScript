package ui;


import java.awt.Rectangle;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import file.FileEx;

public class Init {
	
	private static final String frameName = "Wurstpack Updater";
	private static final Rectangle frameBounds = new Rectangle(500, 500, 500, 300);
	private static final Rectangle updateButtonBounds = new Rectangle(200, 100, 100, 30);
	private static final Rectangle progressBarBounds = new Rectangle(10, 210, 465, 40);
	
	private static JFrame frame;
	private static JButton updateButton;
	private static JProgressBar progressBar;
	
	public static void initNormal() {
		frame = new JFrame(frameName);
		frame.setBounds(frameBounds);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(null);
		frame.setResizable(false);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		updateButton = getUpdateButton();
		frame.add(updateButton);
		
		progressBar = getProgressBar();
		frame.add(progressBar);
		
		frame.setVisible(true);
	}
	
	public static void initSilent() {
		LinkedList<FileEx> toChange = null;
		LinkedList<FileEx> toDelete = null;
		LinkedList<FileEx> toAsk = null;
		try {
			toChange = CheckChanges.getFilesToChange();
			toDelete = CheckChanges.getFilesToDelete();
			toAsk = CheckChanges.getFilesToAsk(toChange, toDelete);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( toChange.size()+toDelete.size()+toAsk.size() > 0 ) {
			initNormal();
		}else{
			return;
		}
			
	}
	
	public static JButton getUpdateButton(){
		JButton temp = new JButton("Update");
		temp.setBounds(updateButtonBounds);
		temp.addActionListener(new UpdateButtonListener());
		return temp;
	}
	
	
	public static JProgressBar getProgressBar(){
		JProgressBar temp = new JProgressBar();
		temp.setBounds(progressBarBounds);
		temp.setStringPainted(true);
		temp.setString("Ready");
		return temp;
	}
	
	public static void setMaxProgress(int max){
		progressBar.setMaximum(max);
	}
	
	public static void setProgress(int current){
		progressBar.setValue(current);
	}
	
	public static void setProgress(String state){
		progressBar.setString(state);
	}


}
