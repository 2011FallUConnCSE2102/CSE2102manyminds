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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import manyminds.datamodel.RaterModel;
import manyminds.util.ManyMindsResolver;
import manyminds.util.XMLFactory;
import manyminds.util.gui.ArtifactLabel;
import manyminds.util.gui.RaterContainer2;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class
ArtifactFactory 
extends XMLFactory {
    
    private static class
    ArtifactPrototypeLoader
    extends DefaultHandler {
	private ArtifactPagePrototype currentPrototype;
        private ArtifactPagePrototype.Element currentPageElement;
	private String currentElement;
	private String currentSection;
	private String currentSide;
	private String currentRaterBox;
	private ArtifactLabel currentLabel;
        private StringBuffer currentString = new StringBuffer();
	private RaterContainer2 raterBox;
        
        public void
        startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
	    currentElement = name;
            boolean freshProto = false;
	    if (currentElement.equals("page")) {
                currentPrototype = new ArtifactPagePrototype();
                currentPrototype.setLongTitle(atts.getValue("long-name"));
                currentPrototype.setShortTitle(atts.getValue("short-name"));
                currentPrototype.setUnselectedColor(new Color(Integer.parseInt(atts.getValue("unselected-color"),16)));
                currentPrototype.setGrowable(Boolean.valueOf(atts.getValue("growable")).booleanValue());
	    } else if (currentElement.equals("top-section")) {
                currentSection = currentElement;
                currentPrototype.setTopWeight(parseDouble(atts.getValue("weight")));
                currentPrototype.setTopColor(new Color(Integer.parseInt(atts.getValue("color"),16)));
            } else if (currentElement.equals("middle-section")) {
                currentSection = currentElement;
                currentPrototype.setMiddleWeight(parseDouble(atts.getValue("weight")));
                currentPrototype.setMainColor(new Color(Integer.parseInt(atts.getValue("color"),16)));
	    } else if (currentElement.equals("bottom-section")) {
                currentSection = currentElement;
                currentPrototype.setBottomWeight(parseDouble(atts.getValue("weight")));
                currentPrototype.setBottomColor(new Color(Integer.parseInt(atts.getValue("color"),16)));
	    } else if (currentElement.equals("left-side")) {
                currentSide = currentElement;
            } else if (currentElement.equals("right-side")) {
                currentSide = currentElement;
	    } else if (currentElement.equals("label")) {
                currentPageElement = new ArtifactPagePrototype.Label();
                freshProto = true;
	    } else if (currentElement.equals("text")) {
                currentString = new StringBuffer();
	    } else if (currentElement.equals("tooltip")) {
                currentString = new StringBuffer();
	    } else if (currentElement.equals("agent-face")) {
                currentPageElement = new ArtifactPagePrototype.AgentFace();
                currentString = new StringBuffer();
                freshProto = true;
	    } else if (currentElement.equals("url")) {
                currentString = new StringBuffer();
	    } else if (currentElement.equals("raterbox")) {
                currentRaterBox = atts.getValue("id");
                currentPageElement = new ArtifactPagePrototype.RaterBox();
                freshProto = true;
	    } else if (currentElement.equals("history")) {
                currentPageElement = new ArtifactPagePrototype.History();
                freshProto = true;
	    } else if (currentElement.equals("history-watch")) {
                String id = atts.getValue("id");
                String tooltip = atts.getValue("tooltip");
                ((ArtifactPagePrototype.History)currentPageElement).addGlobal(id,tooltip);
	    } else if (currentElement.equals("table")) {
                currentPageElement = new ArtifactPagePrototype.Table();
                freshProto = true;
	    } else if (currentElement.equals("textarea")) {
                currentPageElement = new ArtifactPagePrototype.TextArea();
                freshProto = true;
	    } else if (currentElement.equals("textfield")) {
                currentPageElement = new ArtifactPagePrototype.TextField();
                freshProto = true;
	    }
            if (freshProto) {
                String id = atts.getValue("id");
                String tooltip = atts.getValue("tooltip");
                String height = atts.getValue("height");
                String weight = atts.getValue("weight");
                String index = atts.getValue("index");
                currentPageElement.setSide(currentSide);
                currentPageElement.setSection(currentSection);
                if (id != null) {
                    currentPageElement.setDocumentTitle(id);
                }
                if (index != null) {
                    if (index.equals("n") || index.equals("N")) {
                        currentPageElement.setIndex(-1);
                    } else {
                        currentPageElement.setIndex(parseInt(index));
                    }
                }
                if (height != null) {
                    currentPageElement.setHeight(parseInt(height));
                } else {
                    currentPageElement.setHeight(1);
                }
                if (weight != null) {
                    currentPageElement.setWeight(parseDouble(weight));
                } else {
                    currentPageElement.setWeight(1.0);
                }
                if (tooltip != null) {
                    if (!tooltip.equals("--no-tt--")) {
                        currentPageElement.setToolTip(tooltip);
                    }
                }
            }
	}
	
	public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
	    if (name.equals("label")) {
                addToPage(currentPageElement);
            } else if (name.equals("raterbox")) {
                addToPage(currentPageElement);
            } else if (name.equals("history")) {
                addToPage(currentPageElement);
            } else if (name.equals("agent-face")) {
                addToPage(currentPageElement);
            } else if (name.equals("table")) {
                addToPage(currentPageElement);
            } else if (name.equals("textarea")) {
                addToPage(currentPageElement);
            } else if (name.equals("textfield")) {
                addToPage(currentPageElement);
	    } else if (currentElement.equals("text")) {
                currentPageElement.setDocumentTitle(currentString.toString());
	    } else if (currentElement.equals("tooltip")) {
                currentPageElement.setToolTip(currentString.toString());
	    } else if (currentElement.equals("url")) {
                Method m = null;
                try {
                    m = currentPageElement.getClass().getMethod("setLinkTarget",new Class[] {String.class});
                    Object[] invokeArgs = new Object[1];
                    if ("".equals(currentString.toString())) {
                        invokeArgs[0] = null;
                    } else {
                        invokeArgs[0] = currentString.toString();
                    }
                    m.invoke(currentPageElement,invokeArgs);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
	}
	
	public void
	characters(char[] ch, int start, int length) {
            String chars = (new String(ch,start,length)).trim();
            if (chars.length() > 0) {
                currentString.append(chars);
            }
	}
        
                    
        private void
        addToPage(ArtifactPagePrototype.Element e) {
            currentPrototype.addElement(e);
        }    
    }
    
    private static class
    RaterPrototypeLoader
    extends DefaultHandler {
        private int currentIndex;
	private String currentElement;
        private RaterModel currentModel;
        private boolean currentPlural;
        private List currentList;
        private StringBuffer currentString = new StringBuffer();
        private boolean loadingSingle = false;
        
	public
	RaterPrototypeLoader() {}
        
        public
        RaterPrototypeLoader(RaterModel rm) {
            currentModel = rm;
            loadingSingle = true;
        }

	public void
        startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            currentString = new StringBuffer();
	    if (name.equals("rater")) {
                if (!loadingSingle) {
                    currentModel = RaterModel.instantiateRaterModel(null);
                }
                currentModel.setID(atts.getValue("id"));
            } else if (name.equals("rater-title")) {
                currentModel.setTitle("");
            } else if (name.equals("rater-detail")) {
                currentModel.setTitleToolTip("");
            } else if (name.equals("rating-summaries")) {
                currentModel.getSummaries().removeAllElements();
                currentList = new LinkedList();
            } else if (name.equals("rating-details")) {
                currentModel.getPluralToolTips().removeAllElements();
                currentModel.getSingularToolTips().removeAllElements();
                currentList = new LinkedList();
            } else if (name.equals("summary")) {
            } else if (name.equals("url")) {
            } else if (name.equals("rating-detail")) {
            }
	}
 
	public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
	    if (name.equals("rater")) {
                RaterModel.addRaterPrototype(currentModel.getID(),currentModel);
            } else if (name.equals("rater-title")) {
                currentModel.setTitle(currentString.toString());
            } else if (name.equals("rater-detail")) {
                currentModel.setTitleToolTip(currentString.toString());
            } else if (name.equals("rating-summaries")) {
                currentModel.getSliderModel().setMaximum(currentList.size() - 1);
                Iterator it = currentList.iterator();
                while (it.hasNext()) {
                    currentModel.getSummaries().addElement(it.next());
                }
            } else if (name.equals("rating-details")) {
                Iterator it = currentList.iterator();
                while (it.hasNext()) {
                    currentModel.getSingularToolTips().addElement(it.next());
                }
            } else if (name.equals("url")) {
                currentModel.setLinkTarget(currentString.toString());
            } else if (name.equals("summary")) {
                currentList.add(currentString.toString());
            } else if (name.equals("rating-detail")) {
                currentList.add(currentString.toString());
            }
        }
        
        public void
        characters(char[] ch, int start, int length) {
            String s = (new String(ch,start,length)).trim();
            if (s.length() > 0) {
                currentString.append(s);
            }
        }
 
    }
    
    public static void
    loadRaterPrototype(InputSource is) 
    throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new RaterPrototypeLoader());
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(is);
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
    }
    
    public static RaterModel
    loadRaterPrototype(URL desc) 
    throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            RaterModel rm = RaterModel.instantiateRaterModel(null);
            p.setContentHandler(new RaterPrototypeLoader(rm));
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(desc.toString());
            return rm;
        } catch (SAXException se) {
            handleError(se);
            return null;
        } catch (ParserConfigurationException pce) {
            handleError(pce);
            return null;
        }
    }
    
    
    public static void
    loadRaterPrototypes(URL desc)
        throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new RaterPrototypeLoader());
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(desc.toString());
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
   }
	
    public static void
    loadPagePrototype(InputSource is)
    throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new ArtifactPrototypeLoader());
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(is);
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
   }
	
    public static void
    loadPagePrototypes(URL desc)
    throws java.io.IOException {
        try {
            XMLReader p = XMLFactory.createXMLReader(false);
            p.setContentHandler(new ArtifactPrototypeLoader());
            p.setEntityResolver(new ManyMindsResolver());
            p.parse(desc.toString());
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
   }
	
    public static void
    writeRaterModel(File f, RaterModel rm) throws IOException {
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(rm.toXML().getBytes());
        fos.flush();
        fos.close();
    }
}
