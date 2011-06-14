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
 package manyminds.knowledgebase;

/*
 * KnowledgeFactory.java
 *
 *
 * Written by Eric Eslinger
 * Copyright © 1998-2001 University of California
 * All Rights Reserved.
 *
 * Agenda
 *
 * History
 */
 
 
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;

import manyminds.datamodel.Data;
import manyminds.datamodel.DataContext;
import manyminds.datamodel.ManyMindsDocument;
import manyminds.datamodel.RaterModel;
import manyminds.util.ManyMindsResolver;
import manyminds.util.XMLFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class
KnowledgeFactory
extends XMLFactory {

    private static XMLReader KOStringParser;
    private static KnowledgeObjectHandler KOStringHandler;
    private static XMLReader KOStringWithKBParser;
    private static KnowledgeDocumentHandler KOStringWithKBHandler;
    static {
        try {
            KOStringParser = XMLFactory.createXMLReader(false);
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
        KOStringHandler = new KnowledgeObjectHandler();
        KOStringParser.setContentHandler(KOStringHandler);
        KOStringParser.setEntityResolver(new ManyMindsResolver());
        try {
            KOStringWithKBParser = XMLFactory.createXMLReader(false);
        } catch (SAXException se) {
            handleError(se);
        } catch (ParserConfigurationException pce) {
            handleError(pce);
        }
        KOStringWithKBHandler = new KnowledgeDocumentHandler();
        KOStringWithKBParser.setContentHandler(KOStringWithKBHandler);
        KOStringWithKBParser.setEntityResolver(new ManyMindsResolver());
    }
    
    public static
    KnowledgeObject
    fromXML(String s) {
        try {
            KOStringParser.parse(new InputSource(new StringReader(s)));
            return KOStringHandler.getObject();
        } catch (SAXException se) {
            handleError(se);
            return null;
        } catch (java.io.IOException ioe) {
            return null;
        }
    }

    public static
    KnowledgeObject
    fromXML(String s, KnowledgeBase kb) {
        try {
            KOStringWithKBHandler.setKnowledgeBase(kb);
            KOStringWithKBParser.parse(new InputSource(new StringReader(s)));
            return KOStringWithKBHandler.getObject();
        } catch (SAXException se) {
            handleError(se);
            return null;
        } catch (java.io.IOException ioe) {
            return null;
        }
    }

    public static class
    KnowledgeHandler
    extends DefaultHandler {
        protected String currentElement;
        protected Rule currentRule;
        protected Comparator currentGlobal;
        protected KnowledgeObject currentObject;
        protected StringBuffer currentString;
                
        public
        KnowledgeHandler() {}
        
        public void
        startElement(String namespaceURI, String localName, String qName, Attributes atts) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            KnowledgeObject oko = currentObject;
            currentObject = null;
            if (name.equals("advice-give-action")) {
                currentObject = new GiveAdvice();
            } else if (name.equals("belief-assertion")) {
                currentObject = new BeliefAssertion();
            } else if (name.equals("set-global")) {
                currentObject = new SetGlobal();
            } else if (name.equals("belief-requirement")) {
                currentObject = new BeliefRequirement();
            } else if (name.equals("concept")) {
                currentObject = new Concept();
            } else if (name.equals("team-action")) {
                if (atts.getValue("type").equals("form")) {
                        currentObject = new TeamFormationAction();
                }
            } else if (name.equals("add-section")) {
                String w = atts.getValue("weight");
                String n = atts.getValue("name");
                String c = atts.getValue("color");
                String uc = atts.getValue("unselected-color");
                currentObject = new AddSection(n,w,c,uc);
            } else if (name.equals("add-pages")) {
                String lay = atts.getValue("layout");
                String sec = atts.getValue("section");
                currentObject = new AddPages(lay,sec);
            } else if (name.equals("add-rater")) {
                currentObject = new AddRater();
            } else if (name.equals("remove-rater")) {
                currentObject = new RemoveRater();
            } else if (name.equals("global")) {
                currentObject = new Global();
                if (atts.getValue("type").equals("DOCUMENT")) {
                    ((Global)currentObject).setType(Global.DOCUMENT);
                } else if (atts.getValue("type").equals("RSA")) {
                    ((Global)currentObject).setType(Global.RSA);
                    ((Global)currentObject).setPrototype(atts.getValue("prototype"));
                } else if (atts.getValue("type").equals("CONTEXT")) {
                    ((Global)currentObject).setType(Global.CONTEXT);
                }
                ((Global)currentObject).setGroup(parseBoolean(atts.getValue("group")));
            } else if (name.equals("global-requirement")) {
            } else if (name.equals("comparator")) {
                if (atts.getValue("type").equals("eq")) {
                    currentObject = new EqualsComparator();
                } else if (atts.getValue("type").equals("ne")) {
                    currentObject = new NotEqualsComparator();
                } else if (atts.getValue("type").equals("contains")) {
                    currentObject = new ContainsComparator();
                } else if (atts.getValue("type").equals("gt")) {
                    currentObject = new GreaterThanComparator();
                } else if (atts.getValue("type").equals("lt")) {
                    currentObject = new LessThanComparator();
                } else {
                    throw new IllegalArgumentException("Unknown comparator type");
                }
                currentGlobal = (Comparator)currentObject;
            } else if (name.equals("metaglobal")) {
                if (atts.getValue("type").equals("value")) {
                    currentObject = new ValueMetaglobal();
                } else if (atts.getValue("type").equals("const")) { 
                    currentObject = new ConstMetaglobal();
                } else if (atts.getValue("type").equals("length")) { 
                    currentObject = new LengthMetaglobal();
                } else if (atts.getValue("type").equals("timestamp")) { 
                    currentObject = new TimestampMetaglobal();
                } else {
                    throw new IllegalArgumentException("Unknown metaglobal type");
                }
                if (atts.getValue("side").equals("left")) {
                    currentGlobal.setLeft((Metaglobal)currentObject);
                } else if (atts.getValue("side").equals("right")) {
                     currentGlobal.setRight((Metaglobal)currentObject);
               } else {
                    throw new IllegalArgumentException("Wrong side for metaglobal");
                }
            } else if (name.equals("rule")) {
                currentRule = new Rule();
                currentRule.setType(atts.getValue("type"));
                currentObject = currentRule;
            } else if (name.equals("url")) {				
                currentString = new StringBuffer();
            } else if (name.equals("index")) {
                currentString = new StringBuffer();
            } else if (name.equals("detail")) {
                currentString = new StringBuffer();
            } else if (name.equals("reference")) {
                currentString = new StringBuffer();
            } else if (name.equals("value")) {
                currentString = new StringBuffer();
            } else if (name.equals("face-image")) {
            } else {
                throw new IllegalArgumentException("Tried to parse "+name+" which is not in the dtd");
            }
            if (currentObject != null) {
                if (currentObject instanceof Precondition) {
                    String polar = atts.getValue("polarity");
                    if (polar != null) {
                        ((Precondition)currentObject).setPolarity(polar.equals("true"));
                    }
                    if (currentRule != null) { //we might not be parsing the object within the context of a rule
                        currentRule.addPart(currentObject);
                    }
                } else if (currentObject instanceof Postcondition) {
                    if (currentRule != null) { //we might not be parsing the object within the context of a rule
                        currentRule.addPart(currentObject);
                    }
                }
            } else {
                currentObject = oko;
            }
        }
        
        public void
        endElement(String namespaceURI, String localName, String qName) {
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("index")) {
                currentObject.setIndex(currentString.toString());
            } else if (name.equals("url")) {
                currentObject.setURL(currentString.toString());
            } else if (name.equals("detail")) {
                currentObject.setDetail(currentString.toString());
            } else if (name.equals("value")) {
                currentObject.setValue(new Boolean(currentString.toString()));
            } else if (name.equals("reference")) {
                currentObject.setReference(currentString.toString());
            } else if (name.equals("rule")) {
                currentRule.completedLoading();
            }
        }
        
        public void
        characters(char[] ch, int start, int length) {
            String s = (new String(ch,start,length)).trim();
            currentString.append(s);
        }
    }
    
    public static class
    KnowledgeObjectHandler
    extends KnowledgeHandler {
        
        public
        KnowledgeObjectHandler() {
            currentString = new StringBuffer();
        }
     // hi sweetie!  you are the cutest, and i love you tons!!  lOoK, I aM tHe CoOlEst!  
     // ScHmEiCe!
     // You're a clafoutis!  HAH!
        
        public KnowledgeObject
        getObject() {
            KnowledgeObject ret_val;
            if (currentRule != null) {
                ret_val = currentRule;
                currentRule = null;
            } else if (currentGlobal != null) {
                ret_val = currentGlobal;
                currentGlobal = null;
            } else {
                ret_val = currentObject;
                currentObject = null;
            }
            return ret_val;
        }
    }
    
    public static class
    KnowledgeDocumentHandler
    extends KnowledgeHandler {
        protected KnowledgeBase currentKB;

        public
        KnowledgeDocumentHandler() {
            currentString = new StringBuffer();
        }

        public KnowledgeObject
        getObject() {
            KnowledgeObject ret_val;
            if (currentRule != null) {
                ret_val = currentRule;
                currentRule = null;
            } else if (currentGlobal != null) {
                ret_val = currentGlobal;
                currentGlobal = null;
            } else {
                ret_val = currentObject;
                currentObject = null;
            }
            return ret_val;
        }

        public void
        setKnowledgeBase(KnowledgeBase kb) {
            currentKB = kb;
        }

        public void
        endElement(String namespaceURI, String localName, String qName) {
            super.endElement(namespaceURI, localName, qName);
            String name = localName;
            if ("".equals(localName)) {
                name = qName;
            }
            if (name.equals("belief-requirement")) {
                ((BeliefRequirement)currentObject).setKB(currentKB);
            } else if (name.equals("rule")) {
                currentKB.addRule(currentRule);
            } else if (name.equals("concept")) {
                currentKB.mutexBeliefs(((Concept)currentObject).getBeliefs());
            } else if ((name.equals("metaglobal")) && (!(currentObject instanceof ConstMetaglobal))) {
                ((Metaglobal)currentObject).setGlobal(getGlobal(currentObject.getReference()));
            }
        }
    }
    
    public static Global
    getGlobal(String s) {
        int dashLoc = s.lastIndexOf('-');
        if (dashLoc != -1) {
            String gName = s.substring(0,dashLoc);
            String gInd = s.substring(dashLoc+1);
            Global g = new Global();
            Data d = DataContext.getContext().getSharedData().getData(s);
            if (d == null) {
                d = DataContext.getContext().getGlobalVariables().getData(s);
            }
            if (d == null) {
                if ((!gInd.equals("group")) && (RaterModel.isPrototyped(gName))) {
                    d = RaterModel.instantiateRaterModel(gName);
                } else {
                    d = ManyMindsDocument.newDocument();
                }
                d = DataContext.getContext().getSharedData().addData(s,d);
            }
            if (d instanceof RaterModel) {
                g.setType(Global.RSA);
                g.setPrototype(gName);
            } else if (d instanceof ManyMindsDocument) {
                g.setType(Global.DOCUMENT);
            }
            if (gInd.equals("group")) {
                g.setGroup(true);
            } else {
                g.setGroup(false);
            }
            g.setIndex(s);
            return g;
        } else {
            return null;
        }
    }

    
}