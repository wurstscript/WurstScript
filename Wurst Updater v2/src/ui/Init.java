package ui;

import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Init {
	
	private static final String frameName = "Wurstpack Updater";
	private static final Rectangle frameBounds = new Rectangle(500, 500, 500, 300);
	private static final Rectangle updateButtonBounds = new Rectangle(200, 100, 100, 30);
	private static final Rectangle progressBarBounds = new Rectangle(10, 210, 465, 40);
	
	private static JFrame frame;
	private static JButton updateButton;
	private static JProgressBar progressBar;

	public static void run(){
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
