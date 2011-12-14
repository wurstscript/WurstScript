/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * WurstStatusWindow.java
 *
 * Created on 05.12.2011, 21:30:17
 */
package de.peeeq.wurstscript.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.UIManager;

import de.peeeq.wurstscript.WLogger;
import de.peeeq.wurstscript.utils.Utils;

/**
 *
 * @author Frotty
 */
public class WurstStatusWindow extends javax.swing.JFrame {
	
	private javax.swing.JLabel currentStatus;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel title;
	
    /** Creates new form WurstStatusWindow */	
    public WurstStatusWindow() {
    	super("Progress");
    	
    	BufferedImage image = null;
        try {
            image = ImageIO.read(
                getClass().getResource("wurst.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setIconImage(image);
        

        try {
        	UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(WurstStatusWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        initComponents();
        
        Utils.setWindowToCenterOfScreen(this);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setVisible(true);
    }


    @SuppressWarnings("unchecked")
    private void initComponents() {

    	progressBar = new javax.swing.JProgressBar();
        title = new javax.swing.JLabel();
        currentStatus = new javax.swing.JLabel();
        
        progressBar.setDoubleBuffered(true);
        
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        title.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        title.setText("WurstScript");
        
        currentStatus.setDoubleBuffered(true);
        currentStatus.setText("starting...");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                    .addComponent(currentStatus, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(currentStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(12, Short.MAX_VALUE))
        );

        pack();
    }
    
    

//	@Override
//	public void sendError(CompileError err) {
//		errorWindow.setVisible(true);
//		mainGui.gui = errorWindow;
//		mainGui.gui.sendError(err);
//		errorWindow.requestFocus();
//		dispose();
//		
//	}

//	@Override
	public void sendProgress(String whatsRunningNow, double percent) {
		if (whatsRunningNow != null && whatsRunningNow.length() > 1) {
			WLogger.info(whatsRunningNow);
			
			currentStatus.setText(whatsRunningNow);
		}
		if (percent >= 0.0 && percent <= 1.0) {
			progressBar.setValue((int) (percent * 100));
		}
		
	}

//	@Override
	public void sendFinished() {
		System.out.println("CLOSE");
		dispose();
	}
}
