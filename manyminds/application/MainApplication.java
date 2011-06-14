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
 package manyminds.application;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.*;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;

import manyminds.ManyMindsConstants;
import manyminds.agents.AdvicePanel;
import manyminds.agents.Agent;
import manyminds.agents.AgentContainer;
import manyminds.agents.AgentFactory;
import manyminds.agents.AgentList;
import manyminds.artifact.AdviceRaterPane;
import manyminds.artifact.Artifact;
import manyminds.artifact.ArtifactMenuBar;
import manyminds.artifact.QuitAction;
import manyminds.artifact.Scrapbook;
import manyminds.communication.MessageDispatcher;
import manyminds.datamodel.CSVFormatter;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataServerDispatch;
import manyminds.datamodel.LogListener;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.datamodel.RemoteDataServer;
import manyminds.debug.FileHandler;
import manyminds.debug.Handler;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.debug.XMLFormatter;
import manyminds.helpers.AgentControllerThread;
import manyminds.helpers.ArtifactDispatcher;
import manyminds.helpers.PageDisplayerThread;

import manyminds.util.browser.*;

import manyminds.webserver.ServerThread;

public class
MainApplication
extends AbstractApplication
/*implements MRJAboutHandler, MRJQuitHandler*/ {
    
    private class MySignal {
        private boolean myVal = false;
        public void
        signalValue(boolean b) {
            myVal = b;
            synchronized (this) {
                notifyAll();
            }
        }
        
        public boolean
        getValue() {
            return myVal;
        }
        
        public void
        waitForValue(boolean b)
        throws InterruptedException {
            synchronized (this) {
                while (getValue() != b) {
                    wait();
                }
                return;
            }
        }
    }
    
    private static final boolean forceUpdate = false;
    private MySignal artifactLoaded = new MySignal();
    private AgentContainer agentContainer;
    private AgentControllerThread myACT = null;
    private AboutBox aboutBox = new AboutBox();
    private Object webDisplay;
    private boolean quitting = false;
    private static final String LICENSE_PREFACE = "Although the ManyMinds software is distributed under the GPL, "+
        "the agent packages are not necessarily licensed the same way.  This agent package (which define the " +
        "advice, notebook structure, and slider contents) is subject to the following additional license agreement." +
        "Please read it and agree or disagree.  If you do not agree to the agent package's license terms, it will "+
        "not be downloaded or installed.  If you already have an agent package installed, the ManyMinds core will "+
        "launch with those agents instead.  Otherwise the software will quit.\n";
    
    protected JMenuBar makeMenuBar() {
        JMenuBar myJMB = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem quitItem = new JMenuItem(new QuitAction());
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(quitItem);
        myJMB.add(fileMenu);
        return myJMB;
    }

    protected void
    createNotebook(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                try {
                    artifactLoaded.waitForValue(true);
                    LogListener.setLoading(true);
                    Logger.getLogger("manyminds.debug").log(Level.INFO,"Loading Data");
                    RemoteDataServer ds = DataServerDispatch.getDispatch().newDataServer(s);
                    DataContext.getContext().getSharedData().upstreamConnect(ds);
                    DataContext.getContext().getSharedData().addData("Researcher Information IN *",ManyMindsDocument.newDocument()).setValue("1");
                    DataContext.getContext().getSharedData().addData("Question IN Question",ManyMindsDocument.newDocument()).setValue("1");
                    DataContext.getContext().getSharedData().addData("Hypothesis IN Hypothesize",ManyMindsDocument.newDocument()).setValue("1");
                    DataContext.getContext().getSharedData().addData("Hypothesis Brainstorm IN Hypothesize",ManyMindsDocument.newDocument()).setValue("0");
                    DataContext.getContext().getSharedData().addData("Investigation Detail IN Investigate",ManyMindsDocument.newDocument()).setValue("1");
                    DataContext.getContext().getSharedData().addData("Analysis IN Analyze",ManyMindsDocument.newDocument()).setValue("1");
                    DataContext.getContext().getSharedData().addData("Model IN Model",ManyMindsDocument.newDocument()).setValue("1");
                    DataContext.getContext().getSharedData().addData("Evaluation IN Evaluate",ManyMindsDocument.newDocument()).setValue("1");
                    Logger.getLogger("manyminds.debug").log(Level.INFO,"Done Loading Data");
                    LogListener.setLoading(false);
                    agentContainer.reset();
                    Iterator it = AgentList.getAgentList().agentIterator();
                    while (it.hasNext()) {
                        ((Agent)it.next()).reset();
                    }
                    ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID).getTopLevelAncestor().setVisible(true);
                    ((JFrame)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID).getTopLevelAncestor()).setTitle("Research Notebook: "+s);
                    appFrame.setVisible(false);
                    PageDisplayerThread.pageToDisplay(System.getProperty("manyminds.allagentpage"));
                } catch (Throwable t) {
                    logger.log(Level.SEVERE,"Error binding server",t);
                } finally {
                }
            }
        });
    }
    
    protected void
    bindData(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                try {
                    //showSplashScreen();
                    artifactLoaded.waitForValue(true);
                    LogListener.setLoading(true);
                    RemoteDataServer ds = DataServerDispatch.getDispatch().getDataServer(s);
                    Logger.getLogger("manyminds.debug").log(Level.INFO,"Binding Data");
                    DataContext.getContext().getSharedData().upstreamConnect(ds);
                    Logger.getLogger("manyminds.debug").log(Level.INFO,"Done Binding Data");
                    LogListener.setLoading(false);
                    agentContainer.reset();
                    Iterator it = AgentList.getAgentList().agentIterator();
                    while (it.hasNext()) {
                        ((Agent)it.next()).reset();
                    }
                    appFrame.setVisible(false);
                    ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID).getTopLevelAncestor().setVisible(true);
                    ((JFrame)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID).getTopLevelAncestor()).setTitle("Research Notebook: "+s);
                    PageDisplayerThread.pageToDisplay(System.getProperty("manyminds.allagentpage"));
                } catch (Throwable t) {
                    logger.log(Level.SEVERE,"Error binding server",t);
                } finally {
                    //hideSplashScreen();
                }
            }
        });
    }
    

    public Container
    makeContentPane() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        jp.add(new JLabel("Create new project or load old project?"),c);
        
        JButton newButton = new JButton("New");
        newButton.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                String s = JOptionPane.showInputDialog("Project Name:");
                if (s != null) {
                    createNotebook(s.replace('/',' ').replace(':',' '));
                }
            }
        });
        
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            public void
            actionPerformed(ActionEvent ae) {
                try {
                    String s = (String)JOptionPane.showInputDialog(null,
                                    "Please Select a Project",
                                    "Select",
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    DataServerDispatch.getDispatch().getDataNames(),
                                    "SELECT");
                    /*if ((i >= 0) && (i < DataServerDispatch.getDispatch().getDataNames().length)) {
                        bindData(DataServerDispatch.getDispatch().getDataNames()[i]);
                    }*/
                    if ((s != null) && (!"SELECT".equals(s))) {
                        appFrame.setVisible(false);
                        bindData(s);
                    }
                } catch (java.rmi.RemoteException re) {
                    logger.log(Level.SEVERE,"Error getting data name list",re);
                }
            }
        });
        
        c.gridwidth = GridBagConstraints.RELATIVE;
        jp.add(newButton,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        jp.add(loadButton,c);
        return jp;
    }
    
    protected JFrame
    createArtifactFrame() {
    
        appFrameSize = new Dimension(ManyMindsConstants.FULL_WIDTH_FULL_HEIGHT.width * 2, ManyMindsConstants.FULL_WIDTH_FULL_HEIGHT.height);
        
        Artifact rn = new Artifact();
        ApplicationContext.getContext().addApplicationComponent(ARTIFACT_ID,rn);
        ArtifactDispatcher nbd = new ArtifactDispatcher();
        agentContainer = new AgentContainer();
        ApplicationContext.getContext().addApplicationComponent(AGENT_ID,agentContainer);
        AdvicePanel advicePanel = new AdvicePanel();
        ApplicationContext.getContext().addApplicationComponent(ADVICE_PANEL_ID,advicePanel);
/*        JFrame jf = new DataFrame();
        WindowMenu.addFrame(jf);
        ApplicationContext.getContext().addApplicationComponent(DATA_TOOL_ID,jf);
        jf.setVisible(false);*/
        
       final JFrame scrap = new JFrame();
        scrap.setTitle("Scrapbook");
        Scrapbook sb = new Scrapbook();
        scrap.setContentPane(sb);
        scrap.setSize(ManyMindsConstants.FULL_WIDTH_FULL_HEIGHT);
        scrap.setLocation(new Point(FULL_WIDTH_FULL_HEIGHT.width,0));
        scrap.addWindowListener(new WindowAdapter() {
            public void
            windowClosing(WindowEvent we) {
                scrap.setSize(ManyMindsConstants.FULL_WIDTH_FULL_HEIGHT);
            }
        });
        JMenuBar scrapMB = new JMenuBar();
        JMenu scrapEditMenu = new JMenu("Edit");
        scrapEditMenu.add(sb.getAddAction());
        scrapEditMenu.add(sb.getRemoveAction());
        scrapMB.add(scrapEditMenu);
        scrap.setJMenuBar(scrapMB);
        
        WindowMenu.addFrame(scrap);
        ApplicationContext.getContext().addApplicationComponent(SCRAPBOOK_ID,sb);
        scrap.setVisible(false);
        
        try {
            myACT = new AgentControllerThread();
            File f = new File(System.getProperty("manyminds.agentbase"));
            AgentFactory.loadAgents(f);
        } catch (java.io.IOException ioe) {
            logger.log(Level.SEVERE,"Error loading agents",ioe);
        }
        JFrame arp = new AdviceRaterPane();
        WindowMenu.addFrame(arp);
        arp.setVisible(false);
        MessageDispatcher.startDispatching();
        setSplashProgress("Kickstarting Message Dispatchers");

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        //mainPanel.setOpaque(true);
        mainPanel.add(((Artifact)ApplicationContext.getContext().getApplicationComponent(ManyMindsConstants.ARTIFACT_ID)),BorderLayout.CENTER);
        //mainPanel.add(agentContainer,BorderLayout.NORTH);
        mainPanel.add(advicePanel,BorderLayout.NORTH);

        JSplitPane mainWithDisplay = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainWithDisplay.setContinuousLayout(false);
        mainWithDisplay.setOneTouchExpandable(true);
        mainWithDisplay.setLeftComponent(mainPanel);
        
//        String osName = System.getProperty("os.name");
//        if (osName.startsWith("Mac OS")) {
//            try {
//                System.loadLibrary("WebKitComponent");
//                webDisplay = Class.forName("manyminds.util.browser.WebKitComponent").newInstance();
//                JPanel webViewer = new JPanel() {
//                    public void paint(Graphics g) {
//                        super.paint(g);
//                        try {
//                            Class wkcClass = Class.forName("manyminds.util.browser.WebKitComponent");
//                            Method repaintCocoaMethod = wkcClass.getDeclaredMethod("repaintCocoa",null);
//                            repaintCocoaMethod.invoke(webDisplay, null);
//                        } catch (Throwable t) {
//                            t.printStackTrace();
//                        }
//                    }
//                };
//                webViewer.setLayout(new BorderLayout());
//                webViewer.add((Component)webDisplay, BorderLayout.CENTER);
//                try {
//                    Class webViewControllerClass = Class.forName("manyminds.util.browser.WebViewController");
//                    Class[] parms = {Class.forName("manyminds.util.browser.WebKitComponent")};
//                    Object[] vals = {webDisplay};
//                    Constructor wvcConstructor = webViewControllerClass.getDeclaredConstructor(parms);
//                    Object controller = wvcConstructor.newInstance(vals);
//                    webViewer.add((Component)controller, BorderLayout.NORTH);
//                    mainWithDisplay.setRightComponent(webViewer);
//                    manyminds.util.BrowserLauncher.setWebDisplay(webDisplay);
//                    mainWithDisplay.setDividerLocation(ManyMindsConstants.FULL_WIDTH_FULL_HEIGHT.width);
//                }
//                catch (Throwable t) {
//                    t.printStackTrace();
//                }
//            } catch (Throwable e1) {
//                e1.printStackTrace();
//            }
//        } else {
//            mainWithDisplay.setDividerLocation(1.0);
//        }
        
        CalHTMLPreferences pref = new CalHTMLPreferences();
        pref.setShowTestNavBar(true);
        CalHTMLPane pane = new CalHTMLPane(pref, null, null);
        mainWithDisplay.setRightComponent(pane);
        mainWithDisplay.setDividerLocation(ManyMindsConstants.FULL_WIDTH_FULL_HEIGHT.width);
        webDisplay = pane;
        manyminds.util.BrowserLauncher.setWebDisplay((WebKitComponent) webDisplay);        
        
        final JFrame jf = new JFrame();
        jf.setContentPane(mainWithDisplay);
        jf.setSize(appFrameSize);
        jf.setLocation(appLocation);
        jf.setTitle("Inquiry Island Notebook");
        ArtifactMenuBar jmb = new ArtifactMenuBar();
        WindowMenu wm = new WindowMenu();
        WindowMenu.addFrame(jf);
        jmb.add(wm);
        jf.setJMenuBar(jmb);
        jf.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        jf.setVisible(false);
        jf.addWindowListener(new WindowAdapter() {
            public void
            windowClosing(WindowEvent e) {
                jf.setVisible(false);
                appFrame.setVisible(true);
            }
        });
        nbd.waitForLoad();
        //jmb.resetAddRemoveActions();
        artifactLoaded.signalValue(true);
        return jf;
    }
    
    protected String
    getMainWindowTitle() {
        return "Inquiry Island Project Load";
    }
    
    protected void
    runme(String[] args) {
        
        try {
            Handler csvLog = new FileHandler("%H/manyminds-log.%U.%u.csv");
            csvLog.setFormatter(new CSVFormatter());
            csvLog.setLevel(Level.FINEST);
            Handler errLog = new FileHandler("%H/manyminds-errors.%U.%u.xml");
            errLog.setFormatter(new XMLFormatter());
            errLog.setLevel(Level.WARNING);
            Logger.getLogger("manyminds").addHandler(csvLog);        
            Logger.getLogger("manyminds").addHandler(errLog);        
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        if (System.getProperty("manyminds.agentbase") == null) {
            System.setProperty("manyminds.agentbase",System.getProperty("manyminds.home") + System.getProperty("file.separator") + "Agents");
        }
        
        File agentRoot = new File(System.getProperty("manyminds.agentbase"));
        if ("true".equals(System.getProperty("manyminds.checkupdate"))) {
            float localAgentVersion = 0.0f;
            File agentVersionFile = new File(agentRoot,"agents.ver");
            if (agentVersionFile.exists()) {
                try {
                    BufferedReader in = new BufferedReader(new FileReader(agentVersionFile));
                    String verString = in.readLine();
                    localAgentVersion = Float.parseFloat(verString);
                    in.close();
                } catch (Throwable t) {
                    logger.log(Level.WARNING,"Error getting agent version",t);
                }
            }
            
            float remoteAgentVersion = 0.0f;
            try {
                URL agentVersionURL = new URL(System.getProperty("manyminds.agentserver")+"current.txt");
                BufferedReader in = new BufferedReader(new InputStreamReader(agentVersionURL.openStream()));
                String verString = in.readLine();
                remoteAgentVersion = Float.parseFloat(verString);
                in.close();
            } catch (Throwable t) {
                logger.log(Level.WARNING,"Error getting agent version",t);
            }
            
            if (remoteAgentVersion > localAgentVersion) {
                try {
                    URL remoteAgentList = new URL(System.getProperty("manyminds.agentserver")+remoteAgentVersion+"/agent_list.txt");
                    BufferedReader in = new BufferedReader(new InputStreamReader(remoteAgentList.openStream()));
                    LinkedList agentsForUpdate = new LinkedList();
                    while (in.ready()) {
                        agentsForUpdate.add(in.readLine());
                    }
                    in.close();
                    
                    in = new BufferedReader(new InputStreamReader((new URL(System.getProperty("manyminds.agentserver")+remoteAgentVersion+"/license.txt")).openStream()));
                    StringBuffer licenseAgreement = new StringBuffer(LICENSE_PREFACE);
                    while (in.ready()) {
                        licenseAgreement.append(in.readLine());
                        licenseAgreement.append("\n");
                    }
                    in.close();
                    int accept = JOptionPane.showConfirmDialog(null, licenseAgreement, "Agreement", JOptionPane.YES_NO_OPTION);
                    
                    JOptionPane licensePane = new JOptionPane();
                    
                    
                    if (accept == JOptionPane.YES_OPTION) {
                        
                        showSplashScreen(agentsForUpdate.size());
                        Iterator it = agentsForUpdate.iterator();
                        while (it.hasNext()) {
                            String agentString = it.next().toString();
                            setSplashProgress("Updating Agent File: "+agentString);
                            URL remoteAgent = new URL(System.getProperty("manyminds.agentserver")+remoteAgentVersion+"/"+manyminds.util.NameEncoder.encode(agentString));
                            File agentFile = new File(agentRoot, agentString);
                            /* TODO makeFileFromResource(agentFile.getPath(),remoteAgent); tms*/
                        }
                        hideSplashScreen();
                        try {
                            FileWriter out = new FileWriter(agentVersionFile);
                            String verString = Float.toString(remoteAgentVersion);
                            out.write(verString);
                            out.close();
                        } catch (Throwable t) {
                            logger.log(Level.WARNING,"Error writing agent version",t);
                        }
                    } else {
                        if (localAgentVersion > 0.0f) {
                            JOptionPane.showMessageDialog(null, "License not agreed to, using previously installed agents.");
                        } else {
                            JOptionPane.showMessageDialog(null, "License not agreed to, no agents installed, quitting.");
                            System.exit(0);
                        }
                    }
                } catch (Throwable t) {
                    logger.log(Level.WARNING,"Error getting agent from remote",t);
                }
            }
        }
                
        (new Thread() {
            public void
            run() {
                createArtifactFrame();
            }
        }).start();

        DataServerDispatch.connectOrCreateDispatch();
        
        try {
            (new ServerThread("agent://",28082,100,5)).start();
        } catch (Throwable t) {
            logger.log(Level.SEVERE,"Error starting web service",t);
        }
        super.runme(args);
        
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);
        
        appFrame.pack();
        appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        appFrame.addWindowListener(new WindowAdapter() {
            public void
            windowClosing(WindowEvent we) {
                //if (DataServerDispatch.checkSave()) {
                    System.exit(0);
               // } else {
                //}
            }
        });
    }


    
    public
    MainApplication() {
        super();
        System.err.println();
        if (System.getProperty("os.name").startsWith("Mac OS X")) {
            
            try {
                Class applicationClass = Class.forName("com.apple.eawt.Application");
                final Class applicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
                Object applicationObject = applicationClass.newInstance();

                ClassLoader acLoader = applicationClass.getClassLoader();
//                Class[] interfaces = applicationListenerClass.getInterfaces();
                Class[] interfaces = {applicationListenerClass};              
                Object applicationListenerProxy = Proxy.newProxyInstance(acLoader, interfaces, new InvocationHandler() {
                    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                        if (m.getDeclaringClass().equals(applicationListenerClass)) {
                            if (m.getName().equals("handleQuit")) {
                                System.exit(0);
                                return null;
                            } else {
                                return null;
                            }
                        } else {
                            return m.invoke(proxy,args);
                        }
                    }
                });
                
                Method[] methods = applicationClass.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method.getName().equals("addApplicationListener")) {
                        Object[] args = {applicationListenerProxy};
                        method.invoke(applicationObject, args);
                    }
                }
            }
            catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }   

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void
            run() {
                try {
                    DataServerDispatch.getDispatch().saveAllData();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }));
    }
    
    
    public static void
    main(String[] args) {
        System.setProperty("manyminds.appname","Inquiry Island");
        initializeApplicationFiles();
        makeApplicationDirectory("LogFiles");
        makeApplicationDirectory("Agents");
        makeApplicationDirectory("Autosave");
        makeApplicationDirectory("SavedFiles");
        makeApplicationDirectory("Dictionaries");
        
        try {
            loadProperties();
        } catch (IOException ioe) {
            System.err.println("Error Loading System Properties");
            ioe.printStackTrace();
            System.exit(-1);
        }
        

        (new MainApplication()).runme(args);
        
    }
}