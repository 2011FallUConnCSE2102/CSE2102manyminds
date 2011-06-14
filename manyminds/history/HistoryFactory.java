/*
 * Copyright (C) 1998-2002 Regents of the University of California This file is
 * part of ManyMinds.
 * 
 * ManyMinds is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * ManyMinds is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ManyMinds; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package manyminds.history;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.ProgressMonitor;
import javax.swing.ProgressMonitorInputStream;
import javax.xml.parsers.ParserConfigurationException;

import manyminds.util.ManyMindsResolver;
import manyminds.util.XMLFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class HistoryFactory extends XMLFactory {

	public static java.util.List loadCSVLog(URL desc)
			throws java.io.IOException {
		java.util.List rl = new LinkedList();
		BufferedReader in = new BufferedReader(new InputStreamReader(desc
				.openStream()));
		while (in.ready()) {
			HistoryItem hi = new HistoryItem(in.readLine());
			if (hi.getProperty(HistoryItem.ITEM_TYPE).equals(
					HistoryItem.DOCUMENT_CHANGE)
					&& hi.getProperty(HistoryItem.WHAT_HAPPENED).toString()
							.startsWith("current-artifact-")) {
				hi.setProperty(HistoryItem.ITEM_TYPE, HistoryItem.PAGE_FLIP);
			}
			rl.add(hi);
		}
		return rl;
	}

	private static class DatabaseLoader extends DefaultHandler {
		/*
		 * episode* note)> <!ELEMENT resource (#PCDATA)> <!ATTLIST resource type
		 * CDATA "unknown"> <!ELEMENT episode (#PCDATA)>
		 */
		private StringBuffer currentString = new StringBuffer();

		private JarFile myJar = null;

		private String projectTag = "";

		private String currentPropertyKey = null;

		private HistoryItem currentItem = null;

		private HistoryDatabase myDatabase = null;

		private int currentCount = 0;

		private ProgressMonitor myProgressMonitor;

		public DatabaseLoader(File f, HistoryDatabase sp) throws IOException {
			currentString = new StringBuffer();
			myJar = new JarFile(f);
			projectTag = f.getName();
			myDatabase = sp;
		}

		public void startElement(String namespaceURI, String localName,
				String qName, org.xml.sax.Attributes atts) {
			String name = localName;
			if ("".equals(localName)) {
				name = qName;
			}
			if (name.equals("history-project")) {
			} else if (name.equals("history-database")) {
				myProgressMonitor = new ProgressMonitor(null, "Parsing", "", 0,
						45000);
			} else if (name.equals("history-item")) {
				currentItem = new HistoryItem();
				myProgressMonitor.setProgress(++currentCount);
			} else if (name.equals("property")) {
				currentString = new StringBuffer();
				currentPropertyKey = atts.getValue("key");
			} else if (name.equals("summary")) {
				currentString = new StringBuffer();
			} else if (name.equals("episode")) {
				if (atts.getValue("name").equals("unknown")) {
					currentString = new StringBuffer("Episodes/");
				} else {
					try {
						String s = atts.getValue("name");
						JarEntry je = myJar.getJarEntry("Episodes/" + s);
						BufferedReader in = new BufferedReader(
								new InputStreamReader(myJar.getInputStream(je)));
						String projectString = "EME Fall/Winter 2002-2003";
						String periodString = projectTag.substring(projectTag
								.length() - 1);
						String computerString = projectTag.substring(0,
								projectTag.length() - 1);
						while (in.ready()) {
							HistoryItem newHI = new HistoryItem(in.readLine());
							/*
							 * if
							 * (newHI.getProperty(HistoryItem.FUMFUM).equals(HistoryItem.DOCUMENT_CHANGE) &&
							 * newHI.getProperty(HistoryItem.FUMFUM).startsWith("current-artifact-")) {
							 * newHI.setType(HistoryItem.PAGE_FLIP);
							 */
							newHI.setProperty(HistoryItem.PROJECT,
									projectString);
							newHI.setProperty(HistoryItem.PERIOD, periodString);
							newHI.setProperty(HistoryItem.COMPUTER,
									computerString);
							myDatabase.addItem(newHI);
						}
						in.close();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}

		public void characters(char[] ch, int start, int length) {
			if (length > 0) {
				currentString.append(ch, start, length);
			}
		}

		public void endElement(String namespaceURI, String localName,
				String qName) {
			try {
				String name = localName;
				if ("".equals(localName)) {
					name = qName;
				}
				if (name.equals("history-project")) {
				} else if (name.equals("history-database")) {
					myProgressMonitor.setProgress(45000);
					myProgressMonitor.close();
				} else if (name.equals("resource")) {
				} else if (name.equals("summary")) {
				} else if (name.equals("property")) {
					currentItem.setProperty(currentPropertyKey, currentString
							.toString());
				} else if (name.equals("history-item")) {
					if ("Chunk Comment".equals(currentItem.getProperty("Type"))) {
						String cc = (String) currentItem
								.getProperty(HistoryItem.CHUNK_NAME);
						if ((cc != null)
								&& ("".equals(currentItem
										.getProperty(HistoryItem.COMPUTER)))) {
							int spaceOne = cc.indexOf(" ");
							int spaceTwo = cc.indexOf(" ", spaceOne + 1);
							String computer = cc.substring(0, spaceOne).trim();
							computer = computer.substring(0, 1).toUpperCase()
									+ computer.substring(1);
							String period = cc.substring(spaceOne, spaceTwo)
									.trim();
							currentItem.setProperty(HistoryItem.COMPUTER,
									computer);
							currentItem.setProperty(HistoryItem.PERIOD, period);
						}
					}
					myDatabase.addItem(currentItem);
				} else if (name.equals("episode")) {
				}
			} catch (Throwable t) {
				System.err.println("Choking on " + localName + qName + " : "
						+ currentString.toString());
			}
		}
	}

	public static HistoryDatabase loadHistoryDatabase(File desc)
			throws java.io.IOException {
		try {
			XMLReader p = XMLFactory.createXMLReader(false);
			HistoryDatabase hp = new HistoryDatabase();
			p.setContentHandler(new DatabaseLoader(desc, hp));
			p.setEntityResolver(new ManyMindsResolver());
			URL dataURL = new URL("jar:" + desc.toURL().toString()
					+ "!/data.xml");
			InputStream instream = new BufferedInputStream(
					new ProgressMonitorInputStream(null, "Loading Data",
							dataURL.openStream()));
			InputSource is = new InputSource(instream);
			is.setSystemId(dataURL.toString());
			p.parse(is);
			//p.parse("jar:"+desc.toURL().toString()+"!/data.xml");
			System.err.println("done!");
			return hp;
		} catch (SAXException se) {
			handleError(se);
			return null;
		} catch (ParserConfigurationException pce) {
			handleError(pce);
			return null;
		}
	}

	public static void importProjectFile(File desc, HistoryDatabase hp)
			throws java.io.IOException {
		try {
			XMLReader p = XMLFactory.createXMLReader(false);
			p.setContentHandler(new DatabaseLoader(desc, hp));
			p.setEntityResolver(new ManyMindsResolver());
			p.parse("jar:" + desc.toURL().toString() + "!/data.xml");
		} catch (SAXException se) {
			handleError(se);
		} catch (ParserConfigurationException pce) {
			handleError(pce);
		}
	}
}