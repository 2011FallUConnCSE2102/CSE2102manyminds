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
 package manyminds.artifact;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.DataList;
import manyminds.datamodel.DataTable;
import manyminds.util.ManyMindsResolver;
import manyminds.util.XMLFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.RtfWriter;

public class
ReportGenerator
extends XMLFactory {

    private static class
    ReportRTFHandler
    extends DefaultHandler {
    
        private RtfWriter reportWriter = null;
        private com.lowagie.text.Document reportDocument = null;
        private LinkedList blockIter = null;
        private StringBuffer currentString = new StringBuffer();
        private Font baseFont = new Font(Font.HELVETICA, 12, Font.NORMAL, Color.black);
        private Font h2Font = new Font(Font.HELVETICA, 16, Font.BOLD, Color.black);
        private Font currentFont = baseFont;
        private Stack fontStack = new Stack();
        
        private class EndFontTag {}
        
        public
        ReportRTFHandler() {
        }
        
        public void
        startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            try {
                if (name.equals("report")) {
                    reportDocument = new com.lowagie.text.Document();
                    RtfWriter.getInstance(reportDocument, new FileOutputStream(atts.getValue("folder")));
                    reportDocument.setPageSize(PageSize.LETTER);
                    reportDocument.open();
                    reportDocument.add(new Paragraph("Project Report",new Font(Font.HELVETICA, 20, Font.BOLD, Color.black)));
                } else if (name.equals("image")) {
                    Image i = Image.getInstance(new java.net.URL(atts.getValue("source")));
                    reportDocument.add(i);
                    reportDocument.add(new Paragraph());
                } else if (name.equals("font")) {
                    Font f = null;
                    if (atts.getValue("style").equals("big")) {
                        f = h2Font;
                    }
                    if ((f != null) && (blockIter == null)) {
                        fontStack.push(currentFont);
                        currentFont = f;
                    } else if (f != null) {
                        blockIter.add(f);
                    }
                } else if (name.equals("text")) {
                    currentString = new StringBuffer();
                } else if (name.equals("document-block")) {
                    blockIter = new LinkedList();
                } else if (name.equals("document")) {
                    String docName = atts.getValue("name");
                    if (blockIter == null) {
                        reportDocument.add(new Paragraph(DataContext.getContext().getSharedData().getData(docName).getValue(),currentFont));
                    } else {
                        blockIter.add(new DataList(docName,DataContext.getContext().getSharedData()));
                    }
                } else if (name.equals("table")) {
/*                    String tableName = atts.getValue("name");
                    DataTable pdt = new DataTable(tableName,DataContext.getContext().getSharedData());
                    reportWriter.write("<table cellpadding=\"2\" cellspacing=\"2\" border=\"1\" width=\"100%\">");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.write("<tbody>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    for (int i = 0; i < pdt.getRowCount(); ++i) {
                        reportWriter.write("<tr>");
                        reportWriter.write(Character.LINE_SEPARATOR);
                        for (int j = 0; j < pdt.getColumnCount(); ++j) {
                            reportWriter.write("<td valign=\"Top\">");
                            reportWriter.write(pdt.getValueAt(i,j).toString());
                            reportWriter.write("<br> </td>");
                            reportWriter.write(Character.LINE_SEPARATOR);
                        }
                        reportWriter.write("</tr>");
                        reportWriter.write(Character.LINE_SEPARATOR);
                    }
                    reportWriter.write("</tbody>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.write("</table>");
                    reportWriter.write(Character.LINE_SEPARATOR);*/
                }
            } catch (IOException ioe) {
                handleError(ioe);
            } catch (DocumentException de) {
                handleError(de);
            }
        }

        public void
        characters(char[] ch, int start, int length) {
            if (currentString != null) {
                String s = (new String(ch,start,length)).trim();
                currentString.append(s);
            }
        }

        public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            try {
                if (name.equals("report")) {
                    reportDocument.close();
                } else if (name.equals("document-block")) {
                    int iterCount = -1;
                    Iterator it = blockIter.iterator();
                    while (it.hasNext()) {
                        Object o = it.next();
                        if (o instanceof DataList) {
                            int newCount = ((DataList)o).getSize();
                            if (iterCount == -1) {
                                iterCount = newCount;
                            } else if (iterCount > newCount) {
                                iterCount = newCount;
                            }
                        }
                    }
                    if (iterCount == -1) {
                        iterCount = 1;
                    }
                    for (int i = 0; i < iterCount; ++i) {
                        it = blockIter.iterator();
                        while (it.hasNext()) {
                            Object o = it.next();
                            if (o instanceof DataList) {
                                reportDocument.add(new Paragraph(((Data)((DataList)o).getElementAt(i)).getValue(),currentFont));
                            } else if (o instanceof Font) {
                                fontStack.push(currentFont);
                                currentFont = (Font)o;
                            } else if (o instanceof EndFontTag) {
                                if (!fontStack.empty()) {
                                    currentFont = (Font)fontStack.pop();
                                } else {
                                    currentFont = baseFont;
                                }
                            } else {
                                reportDocument.add(new Paragraph(o.toString(),currentFont));
                            }
                        }
                    }
                    blockIter = null;
                } else if (name.equals("font")) {
                    if (blockIter == null) {
                        if (!fontStack.empty()) {
                            currentFont = (Font)fontStack.pop();
                        } else {
                            currentFont = baseFont;
                        }
                    } else {
                        blockIter.add(new EndFontTag());
                    }
                } else if (name.equals("text")) {
                    if (blockIter == null) {
                        reportDocument.add(new Paragraph(currentString.toString(),currentFont));
                        currentString = null;
                    } else {
                        blockIter.add(currentString.toString());
                        currentString = null;
                    }
                }
            } catch (DocumentException de) {
                handleError(de);
            }
        }
    }
        

    private static class
    ReportHTMLHandler
    extends DefaultHandler {
        private Writer reportWriter = null;
        private StringBuffer currentString = null;
        private File reportFolder = null;
        private LinkedList blockIter = null;
        
        public
        ReportHTMLHandler() {
        }
        
        public void
        startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            try {
                if (name.equals("report")) {
                    reportFolder = new File(atts.getValue("folder"));
                    reportFolder.mkdir();
                    File outFile = new File(reportFolder,"report.html");
                    outFile.createNewFile();
                    reportWriter = new FileWriter(outFile);
                    reportWriter.write("<html><head>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.write("<title>Project Report</title>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.write("</head>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.write("<body bgcolor=#FFFFFF>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                } else if (name.equals("image")) {
                    String imageName = atts.getValue("name");
                    String imageSource = atts.getValue("source");
                    placeImage(imageSource,imageName,reportFolder);
                    reportWriter.write("<center><img src =\""+imageName+"\" /></center>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                } else if (name.equals("text")) {
                    currentString = new StringBuffer();
                } else if (name.equals("document-block")) {
                    blockIter = new LinkedList();
                } else if (name.equals("document")) {
                    String docName = atts.getValue("name");
                    if (blockIter == null) {
                        reportWriter.write(DataContext.getContext().getSharedData().getData(docName).getValue());
                        reportWriter.write(Character.LINE_SEPARATOR);
                    } else {
                        blockIter.add(new DataList(docName,DataContext.getContext().getSharedData()));
                    }
                } else if (name.equals("table")) {
                    String tableName = atts.getValue("name");
                    DataTable pdt = new DataTable(tableName,DataContext.getContext().getSharedData());
                    reportWriter.write("<table cellpadding=\"2\" cellspacing=\"2\" border=\"1\" width=\"100%\">");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.write("<tbody>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    for (int i = 0; i < pdt.getRowCount(); ++i) {
                        reportWriter.write("<tr>");
                        reportWriter.write(Character.LINE_SEPARATOR);
                        for (int j = 0; j < pdt.getColumnCount(); ++j) {
                            reportWriter.write("<td valign=\"Top\">");
                            reportWriter.write(pdt.getValueAt(i,j).toString());
                            reportWriter.write("<br> </td>");
                            reportWriter.write(Character.LINE_SEPARATOR);
                        }
                        reportWriter.write("</tr>");
                        reportWriter.write(Character.LINE_SEPARATOR);
                    }
                    reportWriter.write("</tbody>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.write("</table>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                }
            } catch (IOException ioe) {
                handleError(ioe);
            }
        }

        public void
        characters(char[] ch, int start, int length) {
            if (currentString != null) {
                String s = (new String(ch,start,length)).trim();
                currentString.append(s);
            }
        }

        public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            try {
                if (name.equals("report")) {
                    reportWriter.write("</body></html>");
                    reportWriter.write(Character.LINE_SEPARATOR);
                    reportWriter.close();
                } else if (name.equals("document-block")) {
                    int iterCount = -1;
                    Iterator it = blockIter.iterator();
                    while (it.hasNext()) {
                        Object o = it.next();
                        if (o instanceof DataList) {
                            int newCount = ((DataList)o).getSize();
                            if (iterCount == -1) {
                                iterCount = newCount;
                            } else if (iterCount > newCount) {
                                iterCount = newCount;
                            }
                        }
                    }
                    if (iterCount == -1) {
                        iterCount = 1;
                    }
                    for (int i = 0; i < iterCount; ++i) {
                        it = blockIter.iterator();
                        while (it.hasNext()) {
                            Object o = it.next();
                            if (o instanceof DataList) {
                                reportWriter.write(((Data)((DataList)o).getElementAt(i)).getValue());
                                reportWriter.write(Character.LINE_SEPARATOR);
                            } else {
                                reportWriter.write(o.toString());
                                reportWriter.write(Character.LINE_SEPARATOR);
                            }
                        }
                    }
                    blockIter = null;
                } else if (name.equals("text")) {
                    if (blockIter == null) {
                        reportWriter.write(currentString.toString());
                        reportWriter.write(Character.LINE_SEPARATOR);
                        currentString = null;
                    } else {
                        blockIter.add(currentString.toString());
                        currentString = null;
                    }
                }
            } catch (IOException ioe) {
                handleError(ioe);
            }
        }
    }
    
    public static void
    placeImage(String imageSource, String imageName, File imageFolder)
    throws IOException {
        if (imageSource.indexOf("://") != -1) {
            InputStream is = ManyMindsResolver.resolveResource(imageSource);
            BufferedInputStream bis = new BufferedInputStream(is);
            File outfile = new File(imageFolder,imageName);
            OutputStream os = new FileOutputStream(outfile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            int i = -1;
            do {
                i = bis.read();
                if (i != -1) {
                    bos.write(i);
                }
            } while (i != -1);
            bos.close();
            os.close();
        } else {
/*            Chart chart = DataCommandParser.createChart(imageSource);
            if (chart != null) {
                File outfile = new File(imageFolder,imageName);
                OutputStream os = new FileOutputStream(outfile);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                try {
                    chart.exportJPG(bos);
                } catch (Throwable t) {
                }
                bos.close();
                os.close();
            }*/
        }
    }

    public static void
    generateHTMLReport(InputSource is)
    throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new ReportHTMLHandler());
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(is);
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
    }

    public static void
    generateRTFReport(InputSource is)
    throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new ReportRTFHandler());
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(is);
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
    }


}