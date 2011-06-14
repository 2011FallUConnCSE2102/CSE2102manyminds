/*	File ArtifactFactory.java
 * =============================================================================
 * 
 * Factory class for creating artifact pages and sections from descriptors.
 *
 * Author Eric Eslinger
 * Copyright © 1998-2000 University of California
 * All Rights Reserved.
 * 
 * Agenda
 * 
 * History
 * 31 MAY 00	EME new today (from artifact page and pane)
 * 
 * =============================================================================
 */

/*
 * Copyright (C) 1998-2002 Regents of the University of California This file is
 * part of ManyMinds.
 * 
 * ManyMinds is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * ManyMinds is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ManyMinds; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package manyminds.datamodel;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;

import manyminds.debug.Level;
import manyminds.util.ManyMindsResolver;
import manyminds.util.XMLFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author eric
 * 
 * A factory class that handles the SAX-based parsing of data savefiles.
 * It is currently not very good at gracefully handling logical errors 
 * of the sort where the XML is correct, but the DTD isn't correctly followed.
 * But that kind of error should only happen if you're manually editing
 * a savefile or if there is a bug in the savefile export code.
 *
 */
public class DataFactory extends XMLFactory {

    private static class DataServerLoader extends DefaultHandler {

        private String currentIndex;

        private String currentValue;

        private String currentTypeString;

        private StringBuffer currentString;

        private DataServer myDataServer;

        public DataServerLoader(DataServer ds) {
            myDataServer = ds;
            currentString = new StringBuffer();
        }

        public void startElement(String namespaceURI, String localName,
                String qName, org.xml.sax.Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            //nothing really interesting to see here.  We used to preprocess the 
            //entries, but all that got moved to endElement.
            currentString = new StringBuffer();
            if (name.equals("global")) {
            } else if (name.equals("index")) {
            } else if (name.equals("value")) {
            } else if (name.equals("url")) {
            }
        }

        public void characters(char[] ch, int start, int length) {
            if (length > 0) {
                currentString.append(ch, start, length);
            }
        }

        public void endElement(String namespaceURI, String localName,
                String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("index")) {
                currentIndex = currentString.toString();
            } else if (name.equals("value")) {
                currentValue = currentString.toString();
            } else if (name.equals("url")) {
                currentTypeString = currentString.toString();
            } else if (name.equals("global")) {  //We should have sucessfully created a data entry at this point.
                try {
                    myDataServer.addData(currentIndex, currentValue,
                            currentTypeString);  
                    currentIndex = null;
                    currentValue = null;
                    currentTypeString = null;
                } catch (Throwable t) {
                    logger.log(Level.SEVERE, "Error adding data to server", t);
                    Object[] parms = {currentIndex, currentValue};
                    logger.log(Level.SEVERE, "Error adding data to server",
                            parms);
                }
            }
        }
    }

    /**
     * @author eric
     * Old loader left here in case we want to create a loader that can read the 
     * deprecated save file versions.
     */
    private static class DataLoader extends DefaultHandler {

        private String currentDocument;

        private Data currentObject;

        private Set loadedData;

        private StringBuffer currentString;

        public DataLoader() {
            currentString = new StringBuffer();
            loadedData = new HashSet();
        }

        public void startElement(String namespaceURI, String localName,
                String qName, org.xml.sax.Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("global")) {
                if (atts.getValue("type").equals("DOCUMENT")) {
                    currentObject = ManyMindsDocument.newDocument();
                } else if (atts.getValue("type").equals("RSA")) {
                    currentObject = null;
                }
            } else if (name.equals("index")) {
                currentString = new StringBuffer();
            } else if (name.equals("value")) {
                currentString = new StringBuffer();
            } else if (name.equals("url")) {
                currentString = new StringBuffer();
            }
        }

        public void characters(char[] ch, int start, int length) {
            if (length > 0) {
                currentString.append(ch, start, length);
            }
        }

        public void endElement(String namespaceURI, String localName,
                String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("index")) {
                String s = currentObject.getValue();
                currentObject = DataContext.getContext().getSharedData()
                        .addData(currentString.toString(), currentObject);
                currentObject.setValue(s);
                loadedData.add(currentString.toString());
            } else if (name.equals("value")) {
                currentObject.setValue(currentString.toString());
            } else if (name.equals("url")) {
                currentObject = RaterModel.instantiateRaterModel(currentString
                        .toString());
            } else if (name.equals("dataset")) {
                DataContext.getContext().getSharedData()
                        .compactData(loadedData);
            }
        }
    }

    /**
     * @author eric
     * This one doesn't deal with DataServers, all it does is create a 
     * HashMap of key/value pairs from a dataserver.  It is usually used when you're creating
     * a utility that views data, but doesn't need a "live" version of that data.  For example, 
     * it is used by the data analyzer tool that creates a CSV table of all data stored locally.
     */
    private static class MinimalDataLoader extends DefaultHandler {

        private String currentIndex;

        private String currentValue;

        private HashMap loadedData;

        private StringBuffer currentString;

        public MinimalDataLoader() {
            currentString = new StringBuffer();
            loadedData = new HashMap();
        }

        public void resetData() {
            loadedData = new HashMap();
        }

        public boolean foundEntry(String s) {
            return loadedData.containsKey(s);
        }

        public String getEntry(String s) {
            if (loadedData.containsKey(s)) {
                return loadedData.get(s).toString();
            } else {
                return "";
            }
        }

        public void startElement(String namespaceURI, String localName,
                String qName, org.xml.sax.Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            currentString = new StringBuffer();
        }

        public void characters(char[] ch, int start, int length) {
            if (length > 0) {
                currentString.append(ch, start, length);
            }
        }

        public void endElement(String namespaceURI, String localName,
                String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("index")) {
                currentIndex = currentString.toString();
            } else if (name.equals("value")) {
                currentValue = currentString.toString();
            } else if (name.equals("global")) {
                loadedData.put(currentIndex, currentValue);
            }
        }
    }

    /**
     * Not currently used.  But it could load an older-style save file into a DataServer
     * @param desc the url of the savefile (usually a file://)
     * @throws java.io.IOException
     */
    public static void loadData(URL desc) throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new DataLoader());
            p.setEntityResolver(new ManyMindsResolver());
            logger.log(Level.INFO, "Loading Data");
            p.parse(desc.toString());
            logger.log(Level.INFO, "Done Loading Data");
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
    }

    
    
    /**
     * Load all the data files in a certain directory (using the MinimalDataLoader), and output
     * a CSV formatted table of all the interesting values in those data files.
     * 
     * @param folder The directory that contains all the savefiles to be read
     * @param outfile The file to be created (and overwritten) with the CSV table
     * @param interestingEntries The names of all data entries we're interested in (e.g. hypothesis-0)
     * @throws java.io.IOException on plenty of different file errors.
     */
    public static void tabulateData(File folder, File outfile,
            String[] interestingEntries) throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            MinimalDataLoader mdl = new MinimalDataLoader();
            p.setContentHandler(mdl);
            p.setEntityResolver(new ManyMindsResolver());
            File[] fileList = folder.listFiles();
            BufferedWriter out = new BufferedWriter(new FileWriter(outfile));
            out.write("\"Source File\",\"Class Period\",\"Project Title\"");
            for (int i = 0; i < interestingEntries.length; ++i) {
                out.write(",\"");
                out.write(interestingEntries[i]);
                out.write("\"");
            }
            out.write("\n");
            for (int i = 0; i < fileList.length; ++i) {
                if (!fileList[i].getName().startsWith(".")) {
                    mdl.resetData();
                    System.err.println(fileList[i].getName());
                    try {
                        p.parse("jar:" + fileList[i].toURL().toString()
                                + "!/data.xml");
                    } catch (Throwable t) {
                        System.err.println("ERROR on " + fileList[i]);
                        t.printStackTrace();
                    }
                    StringBuffer lineOut = new StringBuffer();
                    lineOut.append("\"");
                    lineOut.append(fileList[i].getName());
                    lineOut.append("\",");
                    String s = fileList[i].getName();
                    int firstDot = s.indexOf(".") + 1;
                    int secondDot = s.indexOf(".", firstDot);
                    lineOut.append("\"");
                    lineOut.append(s.substring(firstDot, secondDot));
                    lineOut.append("\",");
                    lineOut.append("\"");
                    lineOut.append(s.substring(secondDot + 1));
                    lineOut.append("\",");
                    for (int j = 0; j < interestingEntries.length; ++j) {
                        lineOut.append("\"");
                        if (mdl.foundEntry(interestingEntries[j])) {
                            String entry = mdl.getEntry(interestingEntries[j])
                                    .replace('\n', ' ').replace('"', ' ');
                            if (entry.startsWith("=")) {
                                System.err.println(entry);
                                entry = entry.substring(1);
                            }
                            lineOut.append(entry);
                        } else {
                            lineOut.append("NA");
                        }
                        lineOut.append("\",");
                    }
                    lineOut.append("\n");
                    out.write(lineOut.toString());
                }
            }
            out.close();
            System.err.println("Done");
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        } catch (Throwable t) {
            handleError(t);
        }
    }

    /**
     * The new way to create a DataServer from a save file.  The file contains a data.xml
     * file which contains the database as well as any number of binary resources (images and whatnot)
     * 
     * @param desc The jar file to be opened.
     * @return the contructed DataServer
     * @throws java.io.IOException
     */
    public static RemoteDataServer createDataServer(File desc)
            throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            DataServer ds = new DataServer();
            p.setContentHandler(new DataServerLoader(ds));
            p.setEntityResolver(new ManyMindsResolver());
            p.parse("jar:" + desc.toURL().toString() + "!/data.xml");
            JarFile jf = new JarFile(desc);
            Enumeration e = jf.entries();
            while (e.hasMoreElements()) {
                JarEntry je = (JarEntry) e.nextElement();
                if (!je.getName().equals("data.xml")) {
                    ByteArrayOutputStream dataOut = new ByteArrayOutputStream();
                    BufferedInputStream in = new BufferedInputStream(jf
                            .getInputStream(je));
                    byte[] data = new byte[1024];
                    int len;
                    while ((len = in.read(data, 0, data.length)) != -1) {
                        dataOut.write(data);
                    }
                    data = dataOut.toByteArray();
                    ds.addResource(je.getName(), data);
                }
            }
            return ds;
        } catch (SAXException se) {
            handleError(se);
            return null;
        } catch (ParserConfigurationException pce) {
            handleError(pce);
            return null;
        }
    }

}
