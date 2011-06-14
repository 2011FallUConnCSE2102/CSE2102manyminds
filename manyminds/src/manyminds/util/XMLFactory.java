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
 package manyminds.src.manyminds.util;


import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.*;
import manyminds.debug.*;
import java.util.List;
import java.util.LinkedList;

public class
XMLFactory {
    protected static Logger logger = Logger.getLogger("manyminds");

    public static String
    getXMLHeader(String doctype) {
        OutputStreamWriter osr = new OutputStreamWriter(System.out);
        String encoding = osr.getEncoding();
        try {
            osr.close();
        } catch (Throwable t) {}
        return "<?xml version=\"1.0\" encoding=\""
            + encoding
            + "\"?>\n<!DOCTYPE "
            + doctype
            + " PUBLIC \"manyminds-DTD\" \"manyminds.dtd\" >\n";
    }

    public static double parseDouble(String s) {
        try {
            return Double.valueOf(s).doubleValue();
        } catch (Throwable t) {
            logger.log(Level.INFO,"Error parsing double "+s,t);
            return 1.0;
        }
    }

    public static boolean parseBoolean(String s) {
        try {
            return Boolean.valueOf(s).booleanValue();
        } catch (Throwable t) {
            logger.log(Level.INFO,"Error parsing boolean "+s,t);
            return false;
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
            logger.log(Level.INFO,"Error parsing int "+s,t);
            return 0;
        }
    }

    protected static void
    handleError(Throwable e, boolean logged) {
        if (logged) {
            handleError(e);
        } else {
            while (e != null) {
                if (e instanceof SAXParseException) {
                    SAXParseException spe = (SAXParseException)e;
                    System.err.println(
                                "Parse Exception at line "
                                    +spe.getLineNumber()
                                    +" column "
                                    +spe.getColumnNumber()
                                    +" of resource "
                                    +spe.getPublicId() + " / " +spe.getSystemId());
                    spe.printStackTrace();
                } else {
                    System.err.println("Error parsing XML");
                    e.printStackTrace();
                }
                if (e instanceof SAXException) {
                    e = ((SAXException)e).getException();
                } else {
                    e = null;
                }
            }
        }
    }
    

    protected static void
    handleError (Throwable e) {
        while (e != null) {
            if (e instanceof SAXParseException) {
                SAXParseException spe = (SAXParseException)e;
                logger.log(Level.SEVERE,
                            "Parse Exception at line "
                                +spe.getLineNumber()
                                +" column "
                                +spe.getColumnNumber()
                                +" of resource "
                                +spe.getPublicId() + " / " +spe.getSystemId(),spe);
            } else {
                logger.log(Level.SEVERE, "Error parsing XML", e);
            }
            if (e instanceof SAXException) {
                e = ((SAXException)e).getException();
            } else {
                e = null;
            }
        }
    }
    
    public static XMLReader
    createXMLReader(boolean val)
    throws SAXException, ParserConfigurationException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(val);
        SAXParser saxParser = spf.newSAXParser();
        return saxParser.getXMLReader();
    }
}