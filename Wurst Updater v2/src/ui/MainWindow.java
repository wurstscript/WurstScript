package ui;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class MainWindow extends JFrame {

	private JPanel contentPane;
	public JLabel lblChecking;
	public JProgressBar progressBar;
	public JButton btnQuit;
	public JButton btnUpdate;
	public JButton btnAbout;



	/**
	 * Create the frame.
	 */
	public MainWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException
				| IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		setResizable(false);
		initComponents(this);
		Rectangle screenBounds = getGraphicsConfiguration().getBounds();

		int center_x = screenBounds.x + screenBounds.width / 2;
		int center_y = screenBounds.y + screenBounds.height / 2;

		setLocation(center_x - getWidth() / 2,
				center_y - getHeight() / 2);
	}
	private void initComponents(final JFrame frame) {
		setTitle("WurstPack Updater");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 135);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblChecking = new JLabel("Checking...");
		lblChecking.setHorizontalAlignment(SwingConstants.CENTER);
		lblChecking.setBounds(10, 11, 416, 14);
		contentPane.add(lblChecking);
		progressBar = new JProgressBar();
		progressBar.setBounds(10, 36, 416, 30);
		contentPane.add(progressBar);
		btnQuit = new JButton("Quit");
		btnQuit.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				dispose();
			}
		});
		btnQuit.setBounds(337, 74, 89, 23);
		contentPane.add(btnQuit);
		btnUpdate = new JButton("Update");
		btnUpdate.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				btnUpdate.setEnabled(false);
				new UpdateThread().execute();
			}
		});
		btnUpdate.setEnabled(false);
		btnUpdate.setBounds(238, 74, 89, 23);
		contentPane.add(btnUpdate);
		btnAbout = new JButton("About");
		btnAbout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showMessageDialog(frame, "WurstPack Updater\nBy Crigges");
			}
		});
		btnAbout.setBounds(10, 74, 89, 23);
		contentPane.add(btnAbout);
	}

}
