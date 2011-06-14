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
package manyminds.agents;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.parsers.ParserConfigurationException;

import manyminds.datamodel.RaterModel;
import manyminds.util.ManyMindsResolver;
import manyminds.util.XMLFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class AgentDefinitionFactory extends XMLFactory {

	/**
	 * 
	 * @author Eric M Eslinger
	 */
	private static class NotebookPageHandler extends DefaultHandler {
		private StringBuffer currentString = new StringBuffer();
		private String currentName = "";
		private NotebookPageDefinition currentAttributes = null;
		private AgentDefinition myAgent;
		private String wsText, wsTooltip, wsURL, wsID, wsSize;

		public NotebookPageHandler(AgentDefinition ag) {
			myAgent = ag;
		}

		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			org.xml.sax.Attributes atts) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			currentString = new StringBuffer();
			if (name.equals("attributes")) {
				currentAttributes = new NotebookPageDefinition();
			} else if (name.equals("attribute")) {
				currentName = atts.getValue("name");
			} else if (name.equals("parent")) {
			} else if (name.equals("raters")) {
			} else if (name.equals("rater")) {
			} else if (name.equals("workspaces")) {
			} else if (name.equals("workspace")) {
			} else if (name.equals("notebook-page")) {
				currentAttributes = new NotebookPageDefinition();
			}
		}

		public void endElement(
			String namespaceURI,
			String localName,
			String qName) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			if (name.equals("attribute")) {
				currentAttributes.set(currentName, currentString.toString());
			} else if (name.equals("parent")) {
				currentAttributes.addParent(currentString.toString());
			} else if (name.equals("raters")) {
			} else if (name.equals("rater")) {
				currentAttributes.addRater(currentString.toString());
			} else if (name.equals("text")) {
				wsText = currentString.toString();
			} else if (name.equals("tooltip")) {
				wsTooltip = currentString.toString();
			} else if (name.equals("url")) {
				wsURL = currentString.toString();
			} else if (name.equals("id")) {
				wsID = currentString.toString();
			} else if (name.equals("size")) {
				wsSize = currentString.toString();
			} else if (name.equals("workspaces")) {
			} else if (name.equals("workspace")) {
				currentAttributes.addWorkspace(
					wsText,
					wsTooltip,
					wsURL,
					wsID,
					wsSize);
			} else if (name.equals("notebook-page")) {
				myAgent.addNotebookPage(currentAttributes);
			}
		}

		public void characters(char[] ch, int start, int length) {
			String s = new String(ch, start, length);
			currentString.append(s);
		}
	}

	/**
	 * 
	 * @author Eric M Eslinger
	 */
	private static class NotebookSectionHandler extends DefaultHandler {
		private StringBuffer currentString = new StringBuffer();
		private String currentName = "";
		private NotebookSectionDefinition currentAttributes = null;
		private AgentDefinition myAgent;

		public NotebookSectionHandler(AgentDefinition ag) {
			myAgent = ag;
		}

		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			org.xml.sax.Attributes atts) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			currentString = new StringBuffer();
			if (name.equals("attributes")) {
				currentAttributes = new NotebookSectionDefinition();
			} else if (name.equals("attribute")) {
				currentName = atts.getValue("name");
			} else if (name.equals("notebook-section")) {
				currentAttributes = new NotebookSectionDefinition();
			} else if (name.equals("pages")) {
			} else if (name.equals("page")) {
			}
		}

		public void endElement(
			String namespaceURI,
			String localName,
			String qName) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			if (name.equals("attribute")) {
				currentAttributes.set(currentName, currentString.toString());
			} else if (name.equals("parent")) {
				currentAttributes.addParent(currentString.toString());
			} else if (name.equals("notebook-section")) {
				myAgent.addNotebookSection(currentAttributes);
			} else if (name.equals("pages")) {
			} else if (name.equals("page")) {
				currentAttributes.addNotebookPage(currentString.toString());
			}
		}

		public void characters(char[] ch, int start, int length) {
			String s = new String(ch, start, length);
			currentString.append(s);
		}
	}

	/**
	 * 
	 * @author Eric M Eslinger
	 */
	private static class AgentAttributesHandler extends DefaultHandler {
		private StringBuffer currentString = new StringBuffer();
		private String currentName = "";
		private AgentDefinition myAgent = null;

		public AgentAttributesHandler(AgentDefinition ag) {
			myAgent = ag;
		}

		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			org.xml.sax.Attributes atts) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			currentString = new StringBuffer();
			if (name.equals("attribute")) {
				currentName = atts.getValue("name");
			} else if (name.equals("parent")) {
			}
		}

		public void endElement(
			String namespaceURI,
			String localName,
			String qName) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			if (name.equals("attribute")) {
				myAgent.set(currentName, currentString.toString());
			} else if (name.equals("parent")) {
				myAgent.addParent(currentString.toString());
			}
		}

		public void characters(char[] ch, int start, int length) {
			String s = new String(ch, start, length);
			currentString.append(s);
		}
	}

	/**
	 * 
	 * @author Eric M Eslinger
	 */
	private static class LayoutHandler extends DefaultHandler {

		private StringBuffer currentString = new StringBuffer();
		private Layout myLayout = null;

		public LayoutHandler() {
		}

		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			org.xml.sax.Attributes atts) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			currentString = new StringBuffer();
			if (name.equals("header")) {
			} else if (name.equals("footer")) {
			} else if (name.equals("layout")) {
				String layoutName = atts.getValue("name");
				myLayout = Layout.getLayout(layoutName);
			}
		}

		public void endElement(
			String namespaceURI,
			String localName,
			String qName) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			if (name.equals("header")) {
				myLayout.setHeader(currentString.toString());
			} else if (name.equals("footer")) {
				myLayout.setFooter(currentString.toString());
			} else if (name.equals("parent")) {
				myLayout.setParent(currentString.toString());
			}
		}

		public Layout getCurrentLayout() {
			return myLayout;
		}

		public void characters(char[] ch, int start, int length) {
			String s = new String(ch, start, length);
			currentString.append(s);
		}
	}

	/**
	 * 
	 * @author Eric M Eslinger
	 */
	private static class RaterHandler extends DefaultHandler {

		private StringBuffer currentString = new StringBuffer();
		private AgentDefinition myAgent;
		private RaterDefinition currentRater = null;
		private boolean inGiveAdvice = false;
		private RaterDefinition.Advice currentAdvice = null;
		private RaterDefinition.Value currentValue = null;
		//		private String currentName;

		public RaterHandler(AgentDefinition ag) {
			myAgent = ag;
		}

		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			org.xml.sax.Attributes atts) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			currentString = new StringBuffer();
			if (name.equals("attribute")) {
				//                currentName = atts.getValue("name");
			} else if (name.equals("parent")) {
			} else if (name.equals("rater")) {
				currentRater = new RaterDefinition();
			} else if (name.equals("rater-linktarget")) {
				currentAdvice = new RaterDefinition.Advice();
			} else if (name.equals("values")) {
			} else if (name.equals("value")) {
				currentValue = new RaterDefinition.Value();
			} else if (name.equals("value-text")) {
			} else if (name.equals("value-tt")) {
			} else if (name.equals("value-boxcomment")) {
			} else if (name.equals("value-linktarget")) {
				currentAdvice = new RaterDefinition.Advice();
			} else if (name.equals("multi-advice")) {
			} else if (name.equals("detail")) {
			} else if (name.equals("give-advice")) {
				inGiveAdvice = true;
			} else if (name.equals("url")) {
			}
		}

		public void endElement(
			String namespaceURI,
			String localName,
			String qName) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			if (name.equals("attribute")) {
				//				currentRater.set(currentName,currentString.toString());
			} else if (name.equals("parent")) {
				currentRater.addParent(currentString.toString());
			} else if (name.equals("rater")) {
				myAgent.addRater(currentRater);
			} else if (name.equals("rater-linktarget")) {
				if ((currentString.toString().trim().length() != 0)
					&& (currentAdvice.linkList.size() == 0)) {
					currentAdvice.linkList.add(currentString.toString());
				}
				currentRater.setLinkTarget(currentAdvice);
			} else if (name.equals("values")) {
			} else if (name.equals("value")) {
				currentRater.addValue(currentValue);
			} else if (name.equals("value-text")) {
				currentValue.settingText = currentString.toString();
			} else if (name.equals("value-tt")) {
				currentValue.toolTipText = currentString.toString();
			} else if (name.equals("value-linktarget")) {
				if ((currentString.toString().trim().length() != 0)
					&& (currentAdvice.linkList.size() == 0)) {
					currentAdvice.linkList.add(currentString.toString());
				}
				currentValue.linkedAdvice = currentAdvice;
			} else if (name.equals("multi-advice")) {
			} else if (name.equals("value-boxcomment")) {
				currentAdvice.boxComment = currentString.toString();
			} else if (name.equals("detail")) {
				if (inGiveAdvice) {
					currentAdvice.linkTextList.add(currentString.toString());
				} else {
					currentAdvice.overComment = currentString.toString();
				}
			} else if (name.equals("give-advice")) {
				inGiveAdvice = false;
			} else if (name.equals("url")) {
				currentAdvice.linkList.add(currentString.toString());
			}
		}

		public void characters(char[] ch, int start, int length) {
			String s = new String(ch, start, length);
			currentString.append(s);
		}
	}

	/**
	 * 
	 * @author Eric M Eslinger
	 */
	private static class AgentAdviceHandler extends DefaultHandler {
		private StringBuffer currentString = new StringBuffer();
		private String currentName = "";
		private AdvicePageDefinition currentAttributes = null;
		private AgentDefinition currentAgent = null;

		public AgentAdviceHandler() {
		}

		/**
		 * @param retVal
		 */
		public AgentAdviceHandler(AgentDefinition retVal) {
			currentAgent = retVal;
		}

		public void startElement(
			String namespaceURI,
			String localName,
			String qName,
			org.xml.sax.Attributes atts) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			currentString = new StringBuffer();
			if (name.equals("page")) {
				currentAttributes = new AdvicePageDefinition();
			} else if (name.equals("attribute")) {
				currentName = atts.getValue("name");
			} else if (name.equals("parent")) {
			}
		}

		public void endElement(
			String namespaceURI,
			String localName,
			String qName) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			if (name.equals("attribute")) {
				currentAttributes.set(currentName, currentString.toString());
			} else if (name.equals("parent")) {
				currentAttributes.addParent(currentString.toString());
			} else if (name.equals("page")) {
				currentAgent.addPage(currentAttributes);
			}
		}

		public StaticAttributes getCurrentAttributes() {
			return currentAttributes;
		}

		public void characters(char[] ch, int start, int length) {
			String s = new String(ch, start, length);
			currentString.append(s);
		}
	}

	public static AgentDefinition loadAgentAttributes(File agentFile)
		throws IOException {
		JarFile agentJarFile = new JarFile(agentFile);
		ZipEntry plainXML = agentJarFile.getEntry("plain-xml.xml");
		ZipEntry notebookPages = agentJarFile.getEntry("notebook-pages.xml");
		ZipEntry notebookSections =
			agentJarFile.getEntry("notebook-sections.xml");
		ZipEntry raters = agentJarFile.getEntry("raters.xml");
		ZipEntry agentAttributes =
			agentJarFile.getEntry("agent-attributes.xml");
		ZipEntry agentAdvice = agentJarFile.getEntry("agent-advice.xml");
		AgentDefinition retVal = new AgentDefinition();
		BufferedInputStream instream = null;
		if (plainXML != null) {
			try {
				instream =
					new BufferedInputStream(
						agentJarFile.getInputStream(plainXML));
				StringBuffer bufferedXML = new StringBuffer();
				byte[] buf = new byte[1024];
				int len = 0;
				len = instream.read(buf, 0, buf.length);
				while (len > 0) {
					bufferedXML.append(new String(buf, 0, len));
					len = instream.read(buf, 0, buf.length);
				}
				instream.close();
				retVal.setPlainXML(bufferedXML.toString());
			} catch (IOException ioe) {
				handleError(ioe);
			} finally {
				try {
					instream.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		if (notebookPages != null) {
			try {
				XMLReader p = XMLFactory.createXMLReader(false);
				NotebookPageHandler nph = new NotebookPageHandler(retVal);
				p.setContentHandler(nph);
				p.setEntityResolver(new ManyMindsResolver());
				instream =
					new BufferedInputStream(
						agentJarFile.getInputStream(notebookPages));
				InputSource is = new InputSource(instream);
				is.setSystemId(
					"jar:"
						+ agentFile.toURL().toString()
						+ "!/notebook-pages.xml");
				p.parse(is);
				instream.close();
			} catch (SAXException se) {
				handleError(se);
			} catch (ParserConfigurationException pce) {
				handleError(pce);
			} finally {
				try {
					instream.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		if (notebookSections != null) {
			try {
				XMLReader p = XMLFactory.createXMLReader(false);
				DefaultHandler handler = new NotebookSectionHandler(retVal);
				p.setContentHandler(handler);
				p.setEntityResolver(new ManyMindsResolver());
				instream =
					new BufferedInputStream(
						agentJarFile.getInputStream(notebookSections));
				InputSource is = new InputSource(instream);
				is.setSystemId(
					"jar:"
						+ agentFile.toURL().toString()
						+ "!/notebook-sections.xml");
				p.parse(is);
				instream.close();
			} catch (SAXException se) {
				handleError(se);
			} catch (ParserConfigurationException pce) {
				handleError(pce);
			} finally {
				try {
					instream.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		if (raters != null) {
			try {
				XMLReader p = XMLFactory.createXMLReader(false);
				DefaultHandler handler = new RaterHandler(retVal);
				p.setContentHandler(handler);
				p.setEntityResolver(new ManyMindsResolver());
				instream =
					new BufferedInputStream(
						agentJarFile.getInputStream(raters));
				InputSource is = new InputSource(instream);
				is.setSystemId(
					"jar:" + agentFile.toURL().toString() + "!/raters.xml");
				p.parse(is);
				instream.close();
			} catch (SAXException se) {
				handleError(se);
			} catch (ParserConfigurationException pce) {
				handleError(pce);
			} finally {
				try {
					instream.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		if (agentAttributes != null) {
			try {
				XMLReader p = XMLFactory.createXMLReader(false);
				DefaultHandler handler = new AgentAttributesHandler(retVal);
				p.setContentHandler(handler);
				p.setEntityResolver(new ManyMindsResolver());
				instream =
					new BufferedInputStream(
						agentJarFile.getInputStream(agentAttributes));
				InputSource is = new InputSource(instream);
				is.setSystemId(
					"jar:"
						+ agentFile.toURL().toString()
						+ "!/agent-attributes.xml");
				p.parse(is);
				instream.close();
			} catch (SAXException se) {
				handleError(se);
			} catch (ParserConfigurationException pce) {
				handleError(pce);
			} finally {
				try {
					instream.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		if (agentAdvice != null) {
			try {
				XMLReader p = XMLFactory.createXMLReader(false);
				DefaultHandler handler = new AgentAdviceHandler(retVal);
				p.setContentHandler(handler);
				p.setEntityResolver(new ManyMindsResolver());
				instream =
					new BufferedInputStream(
						agentJarFile.getInputStream(agentAdvice));
				InputSource is = new InputSource(instream);
				is.setSystemId(
					"jar:"
						+ agentFile.toURL().toString()
						+ "!/agent-advice.xml");
				p.parse(is);
				instream.close();
			} catch (SAXException se) {
				handleError(se);
			} catch (ParserConfigurationException pce) {
				handleError(pce);
			} catch (IOException e) {
				handleError(e);
			} finally {
				try {
					instream.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		return retVal;
	}

	public static void saveAgent(AgentDefinition aa, File agentFile) {
		JarOutputStream jos = null;
		try {
			jos =
				new JarOutputStream(
					new BufferedOutputStream(new FileOutputStream(agentFile)));
			ZipEntry plainXML = new ZipEntry("plain-xml.xml");
			ZipEntry notebookPages = new ZipEntry("notebook-pages.xml");
			ZipEntry notebookSections = new ZipEntry("notebook-sections.xml");
			ZipEntry raters = new ZipEntry("raters.xml");
			ZipEntry agentAttributes = new ZipEntry("agent-attributes.xml");
			ZipEntry agentAdvice = new ZipEntry("agent-advice.xml");

			jos.putNextEntry(plainXML);
			jos.write(getXMLHeader("plain-xml").getBytes());
			jos.write(aa.getPlainXML().getBytes());

			jos.putNextEntry(notebookPages);
			jos.write(getXMLHeader("notebook-pages").getBytes());
			jos.write("<notebook-pages>\n".getBytes());
			Iterator it = aa.getNotebookPages().iterator();
			while (it.hasNext()) {
				jos.write(
					((NotebookPageDefinition) it.next()).toXML().getBytes());
			}
			jos.write("</notebook-pages>\n".getBytes());

			jos.putNextEntry(notebookSections);
			jos.write(getXMLHeader("notebook-sections").getBytes());
			jos.write("<notebook-sections>\n".getBytes());
			it = aa.getNotebookSections().iterator();
			while (it.hasNext()) {
				jos.write(
					((NotebookPageDefinition) it.next()).toXML().getBytes());
			}
			jos.write("</notebook-sections>\n".getBytes());

			jos.putNextEntry(raters);
			jos.write(getXMLHeader("raters").getBytes());
			jos.write("<raters>\n".getBytes());
			it = aa.getRaters().iterator();
			while (it.hasNext()) {
				jos.write(((RaterDefinition) it.next()).toXML().getBytes());
			}
			jos.write("</raters>\n".getBytes());

			jos.putNextEntry(agentAttributes);
			jos.write(getXMLHeader("agent-attributes").getBytes());
			jos.write(aa.toXML().getBytes());

			jos.putNextEntry(agentAdvice);
			jos.write(getXMLHeader("advice-pages").getBytes());
			jos.write("<advice-pages>\n".getBytes());
			it = aa.getPages().iterator();
			while (it.hasNext()) {
				jos.write(
					((AdvicePageDefinition) it.next()).toXML().getBytes());
			}
			jos.write("</advice-pages>\n".getBytes());

		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			try {
				jos.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * @param url
	 * @return
	 */
	public static RaterModel loadRaterPrototype(URL url) {
		// TODO Auto-generated method stub
		return null;
	}

}
