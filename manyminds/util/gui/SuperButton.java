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
 package manyminds.util.gui;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;

import manyminds.debug.*;
import manyminds.util.*;
import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.communication.KQMLMessage;
import manyminds.helpers.PageDisplayerThread;

/*    <advice-give-action>
        <url><![CDATA[
        <multi-advice agent="Eva Evaluator">
            <detail>Here are two things that might help you evaluate your model</detail>
            <give-advice>
                <url>HTML/g_disclose_uses_limits.html</url>
                <detail>Read the description of this goal</detail>
            </give-advice>
            <give-advice>
                <url>HTML/s_disclose_uses_limits.html</url>
                <detail>Read the appropriate strategy</detail>
            </give-advice>
        </multi-advice>]]></url>
        <detail>I can help you talk about the improvements your project needs.</detail>
    </advice-give-action>
*/

public class
SuperButton 
extends JButton
implements ActionListener {
    
    private static SuperButton verifying = null;
    
    private static class
    CommandParser
    extends DefaultHandler {
    
        private StringBuffer currentString = new StringBuffer();
        private StringBuffer currentMultiText = new StringBuffer();
        private String globalName = null;
        private String agentName = null;
        private String currentURL = null;
        private String currentDetail = null;
        private boolean inInner = false;
        private boolean inMulti = false;
    
        public void
        startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (verifying != null) {
                currentString = new StringBuffer();
                if ("multi-advice".equals(name)) {
                    inMulti = true;
                    inInner = false;
                } else if ("give-advice".equals(name)) {
                    if (inMulti) {
                        inInner = true;
                    }
                }
            } else {
                if ("set-global".equals(name)) {
                    currentString = new StringBuffer();
                    globalName = atts.getValue("name");
                } else if ("give-advice".equals(name)) {
                    currentString = new StringBuffer();
                    if (inMulti) {
                        inInner = true;
                    }
                } else if ("send-message".equals(name)) {
                    currentString = new StringBuffer();
                } else if ("multi-advice".equals(name)) {
                    currentString = new StringBuffer();
                    currentMultiText = new StringBuffer();
                    agentName = atts.getValue("agent");
                    inMulti = true;
                } else if ("url".equals(name)) {
                    currentString = new StringBuffer();
                } else if ("detail".equals(name)) {
                    currentString = new StringBuffer();
                }
            }
        }
        
        public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            } 
            if (verifying != null) {
                if ("multi-advice".equals(name)) {
                    inMulti = false;
                    inInner = false;
                } else if ("give-advice".equals(name)) {
                    if (inMulti) {
                        inInner = false;
                    }
                } else if ("detail".equals(name)) {
                    if ((inMulti && !inInner) || (!inMulti)) {
                        verifying.setToolTipText(currentString.toString());
                    }
                }
            } else {
                if ("set-global".equals(name)) {
                    Data d = DataContext.getContext().getSharedData().getData(globalName);
                    if (d == null) {
                        d = DataContext.getContext().getGlobalVariables().getData(globalName);
                    }
                    if (d != null) {
                        d.setValue(currentString.toString());
                    }
                } else if ("give-advice".equals(name)) {
                    if (inMulti) {
                        currentMultiText.append("<li><a href=\"");
                        currentMultiText.append(System.getProperty("manyminds.adviceroot"));
                        currentMultiText.append(NameEncoder.encode(agentName));
                        currentMultiText.append("/");
                        currentMultiText.append(currentURL);
                        currentMultiText.append("\">");
                        currentMultiText.append(currentDetail);
                        currentMultiText.append("</a></li><br>");
                    } else {
                        if (!currentURL.startsWith("http")) {
                            PageDisplayerThread.pageToDisplay(System.getProperty("manyminds.adviceroot")+currentURL);
                        } else {
                            PageDisplayerThread.pageToDisplay(currentURL);
                        }
                    }
                    currentURL = null;
                    currentDetail = null;
                } else if ("send-message".equals(name)) {
                    KQMLMessage.deliver(currentString.toString());
                } else if ("multi-advice".equals(name)) {
                    inMulti = false;
                    inInner = false;
                    currentMultiText.append("</ul><p>");
                    try {
                        DataContext.getContext().getSharedData().addData(agentName+" Dispatch",new ManyMindsDocument()).setValue(currentMultiText.toString());
                    } catch (Throwable t) {
                        Logger.getLogger("manyminds").log(Level.WARNING,"Error getting data for dispatch creation",t);
                    }
                    PageDisplayerThread.pageToDisplay(System.getProperty("manyminds.adviceroot")+NameEncoder.encode(agentName)+"/HTML/template.html");
                } else if ("url".equals(name)) {
                    currentURL = currentString.toString();
                } else if ("detail".equals(name)) {
                    if (inMulti && !inInner) {
                        currentMultiText.append(currentString.toString());
                        currentMultiText.append("<p><ul>");
                    } else {
                        currentDetail = currentString.toString();
                    }
                }
            }
        }
        
        public void
        characters(char[] ch, int start, int length) {
            String s = (new String(ch,start,length)).trim();
            currentString.append(s);
        }
    }
    
    private static XMLReader myParser;
    private String myCommand;
    
    static {
        try {
            myParser = XMLFactory.createXMLReader(false);
        } catch (Throwable t) {
            Logger.getLogger("manyminds").log(Level.SEVERE,"Error creating XML reader for super button",t);
        }
        CommandParser cp = new CommandParser();
        myParser.setContentHandler(cp);
        myParser.setEntityResolver(cp);
    }
    
    public
    SuperButton(String text, Icon img, String cmd) {
        super(text,img);
        setCommand(cmd);
        addActionListener(this);
    }
    
    public
    SuperButton(String text, String cmd) {
        this(text,null,cmd);
    }
    
    public
    SuperButton(Icon img, String cmd) {
        this(null,img,cmd);
    }
    
    public
    SuperButton() {
        this(null,null,null);
    }
    
    public void
    setCommand(String s) {
        myCommand = s;
        if (myCommand != null) {
            synchronized (myParser) {
                verifying = this;
                verifyCommand(s);
                verifying = null;
            }
        }
    }
    
    public void
    actionPerformed(ActionEvent ae) {
        if (myCommand != null) {
            (new Thread() {
                public void
                run() {
                    executeCommand(myCommand);
                }
            }).start();
        }
    }

    protected static void
    executeCommand(String s) {
        synchronized (myParser) {
            try {
                myParser.parse(new InputSource(new StringReader(s)));
            } catch (Throwable t) {
                Logger.getLogger("manyminds").log(Level.WARNING,"error parsing command string "+s ,t);
            }
        }
    }
    
    protected static boolean
    verifyCommand(String s) {
        synchronized (myParser) {
            try {
                myParser.parse(new InputSource(new StringReader(s)));
                return true;
            } catch (Throwable t) {
                Logger.getLogger("manyminds").log(Level.WARNING,"error parsing command string "+s ,t);
                return false;
            }
        }
    }            
}