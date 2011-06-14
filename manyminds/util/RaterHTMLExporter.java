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
 package manyminds.util;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import manyminds.datamodel.RaterModel;
import manyminds.debug.*;

public class
RaterHTMLExporter {


    private static FileWriter fos;

    private static class
    RaterHTMLizer
    extends DefaultHandler {
        public RaterHTMLizer(String s)
        throws IOException {
            File f = new File(s);
            System.err.println("making "+f.toURL().toString());
            fos = new FileWriter(f);
            fos.write("<html><head><title>Rater List</title></head><body>\n");
        }
        
        private int currentIndex;
	private String currentElement;
        private RaterModel currentModel;
        private boolean currentPlural;
        
	public void
        startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
	    currentElement = name;
            if (currentElement.equals("rater")) {
                currentModel = RaterModel.instantiateRaterModel(null);
                currentModel.setID(atts.getValue("id"));
            } else if (currentElement.equals("rater-title")) {
                currentModel.setTitle("");
            } else if (currentElement.equals("rater-detail")) {
                currentModel.setTitleToolTip("");
            } else if (currentElement.equals("rating-summaries")) {
                currentModel.getSummaries().removeAllElements();
                int count = parseInt(atts.getValue("count"));
                currentModel.getSliderModel().setMaximum(count-1);
                for (int x = 0; x < count; ++x) {
                    currentModel.getSummaries().addElement(new String());
                }
            } else if (currentElement.equals("rating-details")) {
                currentModel.getPluralToolTips().removeAllElements();
                currentModel.getSingularToolTips().removeAllElements();
                int count = parseInt(atts.getValue("count"));
                for (int x = 0; x < count; ++x) {
                    currentModel.getPluralToolTips().addElement(new String());
                    currentModel.getSingularToolTips().addElement(new String());
                }
            } else if (currentElement.equals("summary")) {
                currentIndex = parseInt(atts.getValue("level"));
            } else if (currentElement.equals("rating-detail")) {
                currentIndex = parseInt(atts.getValue("level"));
                if (atts.getValue("plurality").equals("plural")) {
                    currentPlural = true;
                } else {
                    currentPlural = false;
                }
            }
	}
 
	public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
	    if (name.equals("rater")) {
                try {
                    fos.write("<font size=\"4\" face=\"Comic Sans MS,Arial,Helvetica\">");
                    fos.write(currentModel.getTitleToolTip());
                    fos.write("</font><p>\n");
                    fos.write("<ul>\n");
                    ListModel sums = currentModel.getSummaries();
                    ListModel dets = currentModel.getSingularToolTips();
                    for (int i = 0; i < sums.getSize(); ++i) {
                        fos.write("<h4><font size=\"4\" face=\"Comic Sans MS,Arial,Helvetica\" color=\"#006666\">");
                        fos.write(sums.getElementAt(i).toString());
                        fos.write(":</font><font size=\"4\" face=\"Comic Sans MS,Arial,Helvetica\"> ");
                        fos.write(dets.getElementAt(i).toString());
                        fos.write("<br>\n");
                    }
                    fos.write("</ul><br>\n");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        
        public void
        characters(char[] ch, int start, int length) {
            String s = (new String(ch,start,length)).trim();
            if (s.length() > 0) {
                if (currentElement.equals("rater")) {
                } else if (currentElement.equals("rater-title")) {
                    currentModel.setTitle(currentModel.getTitle() + s);
                } else if (currentElement.equals("rater-detail")) {
                    currentModel.setTitleToolTip(currentModel.getTitleToolTip() + s);
                } else if (currentElement.equals("rating-summaries")) {
                } else if (currentElement.equals("rating-details")) {
                } else if (currentElement.equals("summary")) {
                    currentModel.getSummaries().setElementAt(
                            currentModel.getSummaries().getElementAt(currentIndex).toString() + s,
                            currentIndex);
                } else if (currentElement.equals("rating-detail")) {
                    if (currentPlural) {
                        currentModel.getPluralToolTips().setElementAt(
                                currentModel.getPluralToolTips().getElementAt(currentIndex).toString() + s,
                                currentIndex);
                    } else {
                        currentModel.getSingularToolTips().setElementAt(
                                currentModel.getSingularToolTips().getElementAt(currentIndex).toString() + s,
                                currentIndex);
                    }
                }
            }
        }
 
    }            
    
    public static int parseInt(String s) {
        try {
            if (s.startsWith("0x") || s.startsWith("0X")) {
                return Integer.parseInt(s.substring(2),16);
            } else {
                return Integer.parseInt(s);
            }
        } catch (Throwable t) {
            return 0;
        }
    }

    public static void
    main(String[] args) {
        Handler[] handlers = Logger.getLogger("manyminds").getHandlers();
        for (int i = 0; i < handlers.length; ++i) {
            Logger.getLogger("manyminds").removeHandler(handlers[i]);
        }
        
        Handler errorStream = new ConsoleHandler();
        errorStream.setLevel(Level.WARNING);
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            SAXParser saxParser = spf.newSAXParser();
            XMLReader p = saxParser.getXMLReader();
            JFileChooser jfc = new JFileChooser();
            jfc.setMultiSelectionEnabled(true);
            p.setContentHandler(new RaterHTMLizer(System.getProperty("user.home") + System.getProperty("file.separator") + "output.html"));
            p.setEntityResolver(new ManyMindsResolver());
            int i = jfc.showOpenDialog(null);
            if (i == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = jfc.getSelectedFiles();
                for (int j = 0; j < selectedFiles.length; ++j) {
                    try {
                        System.err.println("Parsing "+selectedFiles[j].toURL().toString());
                        p.parse(selectedFiles[j].toURL().toString());
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            fos.write("</body></html>\n");
            fos.close();
            System.exit(0);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
