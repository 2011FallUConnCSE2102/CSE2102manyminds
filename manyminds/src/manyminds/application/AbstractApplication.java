/*  Copyright (C) 1998-2002 Regents of the University of California
 *  This file is part of ManyMinds.
 *
 *  ManyMinds is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  ManyMinds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ManyMinds; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package manyminds.src.manyminds.application;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import manyminds.ManyMindsConstants;
import manyminds.debug.Logger;
import manyminds.util.ManyMindsResolver;



public abstract class
AbstractApplication
implements ManyMindsConstants {

    protected abstract Container makeContentPane();
    protected Logger logger = Logger.getLogger("manyminds");
    protected Dimension appFrameSize = FULL_WIDTH_FULL_HEIGHT;
    protected Point appLocation = new Point(0,0);
    protected JFrame appFrame;
    protected static JWindow splashScreen = new JWindow();
    protected static JProgressBar progressBar = new JProgressBar();
    protected static JLabel progressLabel = new JLabel("Loading");
    protected JMenuBar makeMenuBar() {
        return null;
    }
    
    protected String getMainWindowTitle() {
        return System.getProperty("manyminds.appname","ManyMinds");
    }
    
    protected static void
    loadProperties(String s) 
    throws IOException {
        Properties p = System.getProperties();
        p.load(new BufferedInputStream(new FileInputStream(s)));
        System.setProperties(p);
    }
    
    protected static void
    makeFileFromResource(String file, String resource)
    throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        BufferedInputStream in = new BufferedInputStream(ManyMindsResolver.resolveResource(resource));
        byte[] b = new byte[512];
        int len;
        while ((len = in.read(b, 0, b.length)) != -1) {
            out.write(b, 0, len);
        }
        try {
            out.close();
            in.close();
        } catch (Throwable t) {}
    }
    
    protected static void
    makeFileFromResource(String file, URL resource)
    throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        BufferedInputStream in = new BufferedInputStream(new ProgressMonitorInputStream(null,"Loading "+file,resource.openStream()));
        byte[] b = new byte[512];
        int len;
        while ((len = in.read(b, 0, b.length)) != -1) {
            out.write(b, 0, len);
        }
        try {
            out.close();
            in.close();
        } catch (Throwable t) {}
    }
    
    protected static void
    makeApplicationDirectory(String directory) {
        String home = System.getProperty("manyminds.home");
        File f = new File(home+System.getProperty("file.separator")+directory);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    protected static void
    initializeApplicationFiles() {
        String homeDirectory = System.getProperty("manyminds.home");
        if (homeDirectory == null) {
            if ((new File(System.getProperty("user.dir") + System.getProperty("file.separator") + System.getProperty("manyminds.appname")).exists())) {
                homeDirectory = System.getProperty("user.dir") + System.getProperty("file.separator") + System.getProperty("manyminds.appname");
            } else {
                homeDirectory = System.getProperty("user.home") + System.getProperty("file.separator") + System.getProperty("manyminds.appname");
            }
            System.setProperty("manyminds.home",homeDirectory);
        }
        File f = new File(homeDirectory);
        try {
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Throwable ioe) {
            //logger.log(Level.SEVERE,"Error Creating ManyMinds home directory",ioe);
            System.err.println("Error Creating ManyMinds home directory");
            ioe.printStackTrace();
        }
        File mmProperties = new File(f,"manyminds.properties");
        try {
            if (!mmProperties.exists()) {
                makeFileFromResource(mmProperties.getCanonicalPath(),"classpath://manyminds/resources/manyminds.properties");
            }
        } catch (IOException ioe) {
            //logger.log(Level.SEVERE,"Error Creating ManyMinds properties file",ioe);
            System.err.println("Error Creating ManyMinds properties file");
            ioe.printStackTrace();
        }
    }
    
    protected static void
    loadProperties()
    throws IOException {
        String homeDirectory = System.getProperty("manyminds.home");
        String proploc = System.getProperty("manyminds.home")+System.getProperty("file.separator")+"manyminds.properties";
        loadProperties(proploc);
    }
        

    protected
    AbstractApplication() {
        this(true);
    }
    
    protected
    AbstractApplication(boolean initui) {
        System.err.println("ManyMinds, Copyright (C) 1998-2002 Regents of the University of California");
        System.err.println("Manyminds comes with ABSOLUTELY NO WARRANTY");
        System.err.println("This is free software, and you are welcome to redistribute it under certain conditions");
        System.err.println("See documentation for details");
        if (initui) {
            initUI();
        }
    }
    
    public static void
    showSplashScreen() {
        splashScreen.getContentPane().removeAll();
        splashScreen.getContentPane().add(new JLabel(new ImageIcon(ManyMindsResolver.resolveClasspathURI(System.getProperty("manyminds.splashimage")))));
        splashScreen.pack();
        splashScreen.setVisible(true);
    }
    
    public static void
    showSplashScreen(int tasks) {
        splashScreen.getContentPane().removeAll();
        JPanel jp = new JPanel();
        jp.setLayout(new BorderLayout());
        jp.add(new JLabel(new ImageIcon(ManyMindsResolver.resolveClasspathURI(System.getProperty("manyminds.splashimage")))),BorderLayout.CENTER);
        progressBar.setMinimum(0);
        progressBar.setMaximum(tasks-1);
        progressBar.setValue(0);
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BorderLayout());
        progressPanel.add(progressLabel,BorderLayout.NORTH);
        progressPanel.add(progressBar,BorderLayout.SOUTH);
        jp.add(progressPanel,BorderLayout.SOUTH);
        splashScreen.setContentPane(jp);
        splashScreen.pack();
        splashScreen.setLocation(new Point((Toolkit.getDefaultToolkit().getScreenSize().width - splashScreen.getSize().width) / 2,
            (Toolkit.getDefaultToolkit().getScreenSize().height - splashScreen.getSize().height) / 2));
        splashScreen.setVisible(true);
    }
    
    public static void
    setSplashProgress(String s) {
        progressLabel.setText(s);
        progressBar.setValue(progressBar.getValue() + 1);
    }
    
    public static void
    hideSplashScreen() {
        splashScreen.setVisible(false);
    }
    
    protected static void
    initUI() 
    {
        UIDefaults defaults = UIManager.getDefaults();
        
        if (System.getProperty("os.name").startsWith("Mac OS")) {
            System.setProperty("apple.laf.useScreenMenuBar","true");
            //defaults.put("SliderUI","manyminds.util.gui.mclaf.MultiColorSliderUI");
        } else {
            try {
                UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        
        defaults.put("Button.background", new ColorUIResource(BASE_BACKGROUND));
        defaults.put("Button.foreground", new ColorUIResource(BASE_TEXT));
        defaults.put("Button.font", new FontUIResource(SMALL_FONT));

        defaults.put("Label.font", new FontUIResource(SMALL_FONT));

        defaults.put("List.background", new ColorUIResource(TEXT_BACKGROUND));
        defaults.put("List.foreground", new ColorUIResource(TEXT_TEXT));
        defaults.put("List.selectionForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("List.selectionBackground", new ColorUIResource(TEXT_BACKGROUND));
        
        defaults.put("Menu.background", new ColorUIResource(BASE_BACKGROUND));
        defaults.put("Menu.foreground", new ColorUIResource(BASE_TEXT));
       // defaults.put("Menu.font", new FontUIResource(LARGE_FONT));

        defaults.put("MenuBar.background", new ColorUIResource(BASE_BACKGROUND));
        defaults.put("MenuBar.foreground", new ColorUIResource(BASE_TEXT));
       // defaults.put("MenuBar.font", new FontUIResource(LARGE_FONT));
        
        defaults.put("MenuItem.selectionBackground", new ColorUIResource(BASE_BACKGROUND));
        defaults.put("MenuItem.selectionForeground", new ColorUIResource(BASE_TEXT));
        defaults.put("MenuItem.background", new ColorUIResource(BASE_BACKGROUND));
        defaults.put("MenuItem.foreground", new ColorUIResource(BASE_TEXT));
        defaults.put("MenuItem.acceleratorSelectionForeground", new ColorUIResource(BASE_TEXT));
        defaults.put("MenuItem.acceleratorForeground", new ColorUIResource(BASE_TEXT));
        
        defaults.put("Panel.background", new ColorUIResource(BASE_BACKGROUND));
        defaults.put("Panel.foreground", new ColorUIResource(BASE_TEXT));
        
        defaults.put("Slider.thumb", RATER_BACKGROUND);
        defaults.put("Slider.background", new ColorUIResource(RATER_BACKGROUND));
        defaults.put("Slider.foreground", new ColorUIResource(RATER_STROKE));
        
        /*defaults.put("TabbedPane.selected", new ColorUIResource(HIGHLIGHT_BACKGROUND));
        defaults.put("TabbedPane.background", new ColorUIResource(BASE_SHADED));
        defaults.put("TabbedPane.foreground", new ColorUIResource(BASE_TEXT));*/
        
        defaults.put("Table.background", new ColorUIResource(TEXT_BACKGROUND));
        defaults.put("Table.foreground", new ColorUIResource(TEXT_TEXT));
        defaults.put("Table.focusCellForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("Table.focusCellBackground", new ColorUIResource(TEXT_BACKGROUND));
        defaults.put("Table.selectionForeground", new ColorUIResource(HIGHLIGHT_TEXT));
        defaults.put("Table.selectionBackground", new ColorUIResource(HIGHLIGHT_BACKGROUND));
        defaults.put("Table.gridColor", new ColorUIResource(TEXT_STROKE));
        
        defaults.put("TextArea.background", new ColorUIResource(TEXT_BACKGROUND));
        defaults.put("TextArea.foreground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextArea.inactiveForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextArea.caretForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextArea.selectionBackground", new ColorUIResource(HIGHLIGHT_BACKGROUND));
        defaults.put("TextArea.selectionForeground", new ColorUIResource(HIGHLIGHT_TEXT));
        
        defaults.put("TextField.background", new ColorUIResource(TEXT_BACKGROUND));
        defaults.put("TextField.foreground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextField.inactiveForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextField.caretForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextField.selectionBackground", new ColorUIResource(HIGHLIGHT_BACKGROUND));
        defaults.put("TextField.selectionForeground", new ColorUIResource(HIGHLIGHT_TEXT));
        
        defaults.put("TextPane.background", new ColorUIResource(TEXT_BACKGROUND));
        defaults.put("TextPane.foreground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextPane.inactiveForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextPane.caretForeground", new ColorUIResource(TEXT_TEXT));
        defaults.put("TextPane.selectionBackground", new ColorUIResource(HIGHLIGHT_BACKGROUND));
        defaults.put("TextPane.selectionForeground", new ColorUIResource(HIGHLIGHT_TEXT));
        
        defaults.put("ToolTip.background", new ColorUIResource(TOOLTIP_BACKGROUND));
        defaults.put("ToolTip.foreground", new ColorUIResource(TOOLTIP_TEXT));
        defaults.put("ToolTipUI","manyminds.util.gui.mclaf.MultiColorToolTipUI");
        defaults.put("ToolTip.font", new FontUIResource(LARGE_FONT));
        
        defaults.put("Tree.background", new ColorUIResource(TEXT_BACKGROUND));
        defaults.put("Tree.foreground", new ColorUIResource(TEXT_TEXT));
        
        //defaults.put("TabbedPaneUI","manyminds.util.gui.mclaf.MultiColorTabbedPaneUI");
        
    } // initComponents
        
    protected void
    runme(String[] args) {
        appFrame = new JFrame();
        appFrame.getContentPane().add(makeContentPane());
        appFrame.setSize(appFrameSize);
        appFrame.setLocation(appLocation);
        appFrame.setTitle(getMainWindowTitle());
        appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JMenuBar jmb = makeMenuBar();
        WindowMenu wm = new WindowMenu();
        WindowMenu.addFrame(appFrame);
        if (jmb != null) {
            jmb.add(wm);
            appFrame.setJMenuBar(jmb);
        }
        appFrame.setVisible(true);
    }
    
    protected JFrame
    getAppFrame() {
        return appFrame;
    }
    
} // MainApplet
	