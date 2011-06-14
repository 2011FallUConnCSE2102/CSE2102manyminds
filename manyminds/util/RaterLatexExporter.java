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
import javax.swing.ListModel;
import manyminds.datamodel.RaterModel;

public class
RaterLatexExporter
extends XMLFactory {

    private static class
    RaterLatexifier
    extends DefaultHandler {
        private FileWriter fos;
        public RaterLatexifier(String s)
        throws IOException {
            fos = new FileWriter(s);
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
	    if (currentElement.equals("rater-list")) {
                try {
                    fos.write("\\documentclass[12pt]{article}\n");
                    fos.write("\\usepackage{times}\n");
                    fos.write("\\newif\\ifpdf\n");
                    fos.write("\\ifx\\pdfoutput\\undefined\n");
                    fos.write("\\pdffalse % we are not running PDFLaTeX\n");
                    fos.write("\\else\n");
                    fos.write("\\pdfoutput=1 % we are running PDFLaTeX\n");
                    fos.write("\\pdftrue\n");
                    fos.write("\\fi\n");
                    fos.write("\\ifpdf\n");
                    fos.write("\\usepackage[pdftex]{graphicx}\n");
                    fos.write("\\else\n");
                    fos.write("\\usepackage{graphicx}\n");
                    fos.write("\\fi\n");
                    fos.write("\\textwidth = 6.5 in\n");
                    fos.write("\\textheight = 9 in\n");
                    fos.write("\\oddsidemargin = 0.0 in\n");
                    fos.write("\\evensidemargin = 0.0 in\n");
                    fos.write("\\topmargin = 0.0 in\n");
                    fos.write("\\headheight = 0.0 in\n");
                    fos.write("\\headsep = 0.0 in\n");
                    fos.write("\\parskip = 0.2in\n");
                    fos.write("\\parindent = 0.0in\n");
                    fos.write("\\title{}\n");
                    fos.write("\\author{Eric Eslinger}\n");
                    fos.write("\\begin{document}\n");
                    fos.write("\\ifpdf\n");
                    fos.write("\\DeclareGraphicsExtensions{.pdf, .jpg, .tif}\n");
                    fos.write("\\else\n");
                    fos.write("\\DeclareGraphicsExtensions{.eps, .jpg}\n");
                    fos.write("\\fi\n");
                    fos.write("\n\n\\begin{enumerate}\n");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
	    } else if (currentElement.equals("rater")) {
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
                    fos.write("\\item ");
                    fos.write(currentModel.getTitleToolTip());
                    fos.write("\n");
                    fos.write("\\begin{itemize}\n");
                    ListModel sums = currentModel.getSummaries();
                    ListModel dets = currentModel.getSingularToolTips();
                    for (int i = 0; i < sums.getSize(); ++i) {
                        fos.write("\\item["/*]*/);
                        fos.write(Integer.toString(i));
                        fos.write(": ");
                     //   fos.write(sums.getElementAt(i).toString());
                        fos.write(/*[*/"] ");
                        fos.write(dets.getElementAt(i).toString());
                        fos.write("\n");
                    }
                    fos.write("\\end{itemize}\n");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            } else if (name.equals("rater-list")) {
                try {
                    fos.write("\\end{enumerate}\n\\end{document}\n");
                    fos.close();
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
    
    public static void
    main(String[] args) {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new RaterLatexifier(args[1]));
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(new InputSource(new BufferedInputStream(new FileInputStream(args[0]))));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
