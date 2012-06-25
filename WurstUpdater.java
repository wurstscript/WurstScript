/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wurstupdater;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JOptionPane;

/**
 *
 * @author Frotty
 */
public class WurstUpdater {
    WurstUpdaterWindow ui;
    private final String configPath = "updater.cfg";
    private String root = "";
    private File tempdir;
    private String versionURL = "";
    private String historyURL = "";
    private String updateURL = "";
    private float currentVer;
    private float latestVer;
    private Thread worker;
    
    public WurstUpdater(WurstUpdaterWindow ui) throws Exception {
        this.ui = ui;
        if (!new File(configPath).exists()) {
            downloadConfig("http://sunayama.de/Wurst/updater.cfg");
        }
        String s = new File(configPath).getAbsolutePath();
        
        System.err.println("Wurst");        

        root = (s.substring(0,s.lastIndexOf("\\")));
        root = root.substring(0, root.lastIndexOf("\\"));
        tempdir = new File(root + "\\update\\");
        tempdir.mkdir();

        System.out.println(root);
        versionURL = getSourceSite() + "version.html";
        historyURL = getSourceSite() + "history.html";
        currentVer = Float.parseFloat(getCurrentVersion());
        latestVer = Float.parseFloat(getLatestVersion());
        
        if ( currentVer == 0.0) {
            updateURL = getSourceSite() + "install.zip";
        }else {
            updateURL = getSourceSite() + "update.zip";
        }
        
        System.out.println(getCurrentVersion());
        
        
        ui.sendVersions(currentVer, latestVer);
        ui.sendWhatsNew(getWhatsNew());
        if ( currentVer < latestVer) {
            ui.enableUpdate(true);
        }
        
        
    }
    
    public void update() {
        ui.sendStatus(1,"Contacting Download Server...");
        download();
    }
    
    private String getCurrentVersion() throws Exception
    {
        String data = getData(configPath);
        return data.substring(data.indexOf("[current]")+9,data.indexOf("[/current]"));
    }
    
    private String getSourceSite() throws Exception
    {
        String data = getData(configPath);
        return data.substring(data.indexOf("[source]")+8,data.indexOf("[/source]"));
    }
    
    private String getLatestVersion() throws Exception
    {
        String data = getURLData(versionURL);
        return data.substring(data.indexOf("[version]")+9,data.indexOf("[/version]"));
    }
    
    private String getWhatsNew() throws Exception
    {
        String data = getURLData(historyURL);
        return data.substring(data.indexOf("[history]")+9,data.indexOf("[/history]"));
    }
    
    private String getData(String path) throws IOException
    {
        File f = new File(path);
        
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        
        int c = 0;
        StringBuilder buffer = new StringBuilder("");

        while(c != -1) {
            c = in.read();            
            buffer.append((char)c);
        }
        return buffer.toString();    
    }
    
    private String getURLData(String address) throws IOException
    {
        URL url = new URL(address);
        
        InputStream html = null;

        html = url.openStream();
        
        int c = 0;
        StringBuilder buffer = new StringBuilder("");

        while(c != -1) {
            c = html.read();
            
        buffer.append((char)c);
        }
        return buffer.toString();    
    }
    
    private void download()
    {
        worker = new Thread(
        new Runnable(){
            @Override
            public void run()
            {
                try {
                    ui.sendStatus(10,"Downloading...");
                    downloadFile(updateURL);
                    ui.sendStatus(40,"Extracting...");
                    unzip();
                    ui.sendStatus(40,"Updating Files...");
                    File f = new File(tempdir.getAbsolutePath() + "\\update.zip");
                    f.delete();
                    copyFiles(tempdir,root);
                    ui.sendStatus(90,"Cleaning up...");
                    cleanup();
                    ui.sendStatus(100,"Finished...");
                    ui.enableUpdate(false);
                    ui.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "An error occured while preforming update!");
                }
            }
        });
        worker.start();
    }

    private void cleanup()
    {    
        remove(tempdir);
        tempdir.delete();
    }
    private void remove(File f)
    {
        File[]files = f.listFiles();
        for(File ff:files)
        {
            if(ff.isDirectory())
            {
                remove(ff);
                ff.delete();
            }
            else
            {
                ff.delete();
            }
        }
    }
    private void copyFiles(File f,String dir) throws IOException
    {
        File[]files = f.listFiles();
        for(File ff:files)
        {
            if(ff.isDirectory()){
                new File(dir+"/"+ff.getName()).mkdir();
                copyFiles(ff,dir+"/"+ff.getName());
            }
            else
            {
                copy(ff.getAbsolutePath(),dir+"/"+ff.getName());
            }

        }
    }
    public void copy(String srFile, String dtFile) throws FileNotFoundException, IOException{

          File f1 = new File(srFile);
          File f2 = new File(dtFile);

          InputStream in = new FileInputStream(f1);

          OutputStream out = new FileOutputStream(f2);

          byte[] buf = new byte[1024];
          int len;
          while ((len = in.read(buf)) > 0){
            out.write(buf, 0, len);
          }
          in.close();
          out.close();
      }
    private void unzip() throws IOException
    {
         int BUFFER = 2048;
         BufferedOutputStream dest = null;
         BufferedInputStream is = null;
         ZipEntry entry;
         ZipFile zipfile = new ZipFile(tempdir + "\\update.zip");
         Enumeration e = zipfile.entries();
         while(e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            if(entry.isDirectory())
                (new File(tempdir+"\\"+entry.getName())).mkdir();
            else{
                (new File(tempdir+"\\"+entry.getName())).createNewFile();
                is = new BufferedInputStream
                  (zipfile.getInputStream(entry));
                int count;
                byte data[] = new byte[BUFFER];
                FileOutputStream fos = new
                  FileOutputStream(tempdir+"\\"+entry.getName());
                dest = new
                  BufferedOutputStream(fos, BUFFER);
                while ((count = is.read(data, 0, BUFFER))
                  != -1) {
                   dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
                is.close();
            }
         }
         zipfile.close();

    }
    

        
    private void downloadFile(String link) throws MalformedURLException, IOException
    {
        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        long max = conn.getContentLength();
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File(tempdir + "\\update.zip")));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead = 0;
        int in = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            in += bytesRead;
            fOut.write(buffer, 0, bytesRead);
        }
        fOut.flush();
        fOut.close();
        is.close();
    }
    
    private void downloadConfig(String link) throws MalformedURLException, IOException
    {
        URL url = new URL(link);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        long max = conn.getContentLength();
        BufferedOutputStream fOut = new BufferedOutputStream(new FileOutputStream(new File("updater.cfg")));
        byte[] buffer = new byte[32 * 1024];
        int bytesRead = 0;
        int in = 0;
        while ((bytesRead = is.read(buffer)) != -1) {
            in += bytesRead;
            fOut.write(buffer, 0, bytesRead);
        }
        fOut.flush();
        fOut.close();
        is.close();
    }

}
