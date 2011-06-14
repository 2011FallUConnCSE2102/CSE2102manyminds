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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import manyminds.debug.Level;
import manyminds.history.ChunkList;
import manyminds.history.HistoryChunk;
import manyminds.history.HistoryDatabase;
import manyminds.history.HistoryFactory;
import manyminds.history.SearchingChunker;
import manyminds.history.SimpleChunkingPolicy;




public class
DataReplayApplication
extends AbstractApplication {

    private HistoryDatabase myDatabase;
    private ChunkList myChunkList;

    private static DataReplayApplication SINGLE = null;

    public static boolean checkSave() {
        (new Thread() {
            public void
            run() {
                SINGLE.doSave();
            }
        }).start();
        return true;
    }

    public 
    DataReplayApplication() {
        super(false);
        try {
            quicktime.QTSession.open();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        SINGLE = this;
        appFrameSize = new Dimension(500,400);
        File projectDatabaseFile = new File(System.getProperty("manyminds.home") + System.getProperty("file.separator") + "Database.jar");
        
        if (projectDatabaseFile.exists()) {
            try {
                myDatabase = HistoryFactory.loadHistoryDatabase(projectDatabaseFile);
            } catch (Throwable t) {
                myDatabase = new HistoryDatabase();
                t.printStackTrace();
            }
        } else {
            myDatabase = new HistoryDatabase();
        }
    }
   
    public boolean
    doSave()  {
        File projectDatabaseFile = new File(System.getProperty("manyminds.home") + System.getProperty("file.separator") + "Database.jar");
        JarOutputStream out = null;
        try {
            out = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(projectDatabaseFile)));
            JarEntry currentJarEntry = new JarEntry("data.xml");
            out.putNextEntry(currentJarEntry);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            myDatabase.saveYourself(bos);
            bos.close();
            return true;
        } catch (IOException ioe) {
            if (out != null) {
                try {
                    out.close();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return false;
        }
    }
    
    protected JMenuBar
    makeMenuBar() {
        JMenuBar jmb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem saveAll = new JMenuItem(new AbstractAction("Save All") {
            public void
            actionPerformed(ActionEvent ae) {
                checkSave();
            }
        });
        saveAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem summarizeData = new JMenuItem(new AbstractAction("Summarize Data") {
            public void
            actionPerformed(ActionEvent ae) {
                (new Thread() {
                    public void
                    run() {
                        String s = myDatabase.aggregateTimeData();
                        JFrame jf = new JFrame();
                        JTextArea jta = new JTextArea();
                        jta.setText(s);
                        jf.setContentPane(new JScrollPane(jta));
                        jf.pack();
                        jf.setVisible(true);
                    }
                }).start();
            }
        });
        summarizeData.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem aggregateTime = new JMenuItem(new AbstractAction("Aggregate Time Data") {
            public void
            actionPerformed(ActionEvent ae) {
                (new Thread() {
                    public void
                    run() {
                        String s = myDatabase.aggregateTimeData();
                        JFrame jf = new JFrame();
                        JTextArea jta = new JTextArea();
                        jta.setText(s);
                        jf.setContentPane(new JScrollPane(jta));
                        jf.pack();
                        jf.setVisible(true);
                    }
                }).start();
            }
        });
        aggregateTime.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem printTraces = new JMenuItem(new AbstractAction("Print Traces") {
            public void
            actionPerformed(ActionEvent ae) {
//                (new HistoryPrinter(projectList)).printTraces();
            }
        });
        printTraces.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem searchDatabase = new JMenuItem(new AbstractAction("Search Database") {
            public void
            actionPerformed(ActionEvent ae) {
                SearchingChunker sc = (new SearchingChunker.CriteriaFrame(getAppFrame())).showDialog();
                if (sc != null) {
                    JFrame searchResults = new JFrame();
                    searchResults.setContentPane(new JScrollPane(new ChunkList(sc.chunkData(myDatabase).values())));
                    searchResults.setSize(1074,768);
                    searchResults.setVisible(true);
                }
            }
        });
        searchDatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        JMenuItem importFiles = new JMenuItem(new AbstractAction("Import Old Files") {
            public void
            actionPerformed(ActionEvent ae) {
                (new Thread() {
                    public void
                    run() {
                        File[] files = (new File(System.getProperty("manyminds.home") + System.getProperty("file.separator") + "ProjectFiles")).listFiles();
                        for (int i = 0; i < files.length; ++i) {
                            try {
                                if (!files[i].getName().startsWith(".")) {
                                    try {
                                        System.err.println(files[i].getName());
                                        HistoryFactory.importProjectFile(files[i],myDatabase);
                                    } catch (Throwable t) {
                                        t.printStackTrace();
                                        i = files.length;
                                    }
                                }
                            } catch (Throwable t) {
                                logger.log(Level.WARNING, "Error loading "+files[i].getName(),t);
                            }
                        }
                    }
                }).start();
            }
        });
        file.add(saveAll);
        file.add(printTraces);
        file.add(summarizeData);
        file.add(aggregateTime);
        file.add(importFiles);
        file.add(searchDatabase);
        jmb.add(file);
        return jmb;
    }

    public void
    refreshChunkList() {
        SwingUtilities.invokeLater(new Runnable() {
            public void
            run() {
                Map m = (new SimpleChunkingPolicy()).chunkData(myDatabase);
                myChunkList.removeAll();
                TreeMap sortedMap = new TreeMap(m);
                Iterator it = sortedMap.values().iterator();
                while (it.hasNext()) {
                    myChunkList.addChunk((HistoryChunk)it.next());
                }
                myChunkList.revalidate();
            }
        });
    }

    public Container
    makeContentPane() {
        myChunkList = new ChunkList();
        refreshChunkList();
        return new JScrollPane(myChunkList);        
    }

    public void
    finalize() throws Throwable {
        quicktime.QTSession.close();
        super.finalize();
    }

    public static void
    main(String[] args) {
        System.setProperty("manyminds.appname","ManyMinds Analyzer");
        initializeApplicationFiles();
        makeApplicationDirectory("ProjectFiles");
        makeApplicationDirectory("LogFiles");
        makeApplicationDirectory("VideoFiles");
        makeApplicationDirectory("Autosave");
        
        try {
            loadProperties();
        } catch (IOException ioe) {
            System.err.println("Error Loading System Properties");
            ioe.printStackTrace();
            System.exit(-1);
        }
        
        (new DataReplayApplication()).runme(args);
    }
} // MainApplet
	