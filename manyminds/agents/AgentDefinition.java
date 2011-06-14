package manyminds.agents;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import manyminds.util.NameEncoder;

public class AgentDefinition extends StaticAttributes {

	/**
     * Create a new agent definition.  Can happen in the editor or the factory.
	 * @param n the name of the definition.  Named agents are not in the global repository.
	 */
	public AgentDefinition(String n) {
		super(n);
		set("agent-name", n);
	}

	/**
	 * 
	 */
	public AgentDefinition() {
		super(false);
	}

	/**
	 * Instead of just returning a value, some special keys can cause other
     * kinds of actions.  They are:
     * <li>
     * <ul>copy-resource URL: if the output folder is set, copy a resource from the file URL given into the agent's library (good for putting images into the agent's resource directory) </ul>
     * <ul>get-from-page [key1 val1] lookup: find the first page for which all keyN meet the valN requirements and return the value of lookup in that page's attribute set</ul>
     * <ul>page-exists [key val]...: true if an advice page exists that contains all the key-val mappings listed</ul>
     * <ul>page-list: list all of this agent's advice pages</ul>
     * </li>
	 * @param k the string to look up
	 * @return the value returned.
	 * @see manyminds.agents.Attributes#get(java.lang.String)
	 */
	//here's the stuff dealing with attributes and whatnot

	public String get(String k) {
		try {
			if ((outputFolder != null) && (k.startsWith("copy-resource"))) {
				String resName =
					URLDecoder.decode(k.substring(13).trim(), "UTF-8");
				File resourceFile = new File(resName);
				File newFile = new File(outputFolder, resourceFile.getName());
				if (!newFile.exists()) {
					try {
						FileOutputStream out = new FileOutputStream(newFile);
						BufferedInputStream in =
							new BufferedInputStream(
								new FileInputStream(resourceFile));
						int s = in.read();
						while (s != -1) {
							out.write(s);
							s = in.read();
						}
						out.close();
						in.close();
						return "";
					} catch (Throwable t) {
						t.printStackTrace();
						return null;
					}
				} else {
					return "";
				}
			} else if (k.startsWith("get-from-page")) {
				StringTokenizer st = new StringTokenizer(k);
				st.nextToken();
				String lookup = null;
				HashMap reqs = new HashMap();
				while (st.hasMoreTokens()) {
					String key = st.nextToken();
					if (st.hasMoreTokens()) {
						String val = st.nextToken();
						reqs.put(
							URLDecoder.decode(key, "UTF-8"),
							URLDecoder.decode(val, "UTF-8"));
					} else {
						lookup = key;
					}
				}
				Iterator it = myPages.iterator();
				while (it.hasNext()) {
					boolean matched = true;
					AdvicePageDefinition page =
						(AdvicePageDefinition) it.next();
					Iterator keys = reqs.keySet().iterator();
					while (matched && keys.hasNext()) {
						String key = keys.next().toString();
						String val = reqs.get(key).toString();
						if (!val.equals(page.get(key))) {
							matched = false;
						}
					}
					if (matched) {
						return page.get(lookup);
					}
				}
				return "";
			} else if (k.startsWith("page-exists")) {
				StringTokenizer st = new StringTokenizer(k);
				st.nextToken();
				HashMap reqs = new HashMap();
				while (st.hasMoreTokens()) {
					String key = st.nextToken();
					String val = st.nextToken();
					reqs.put(
						URLDecoder.decode(key, "UTF-8"),
						URLDecoder.decode(val, "UTF-8"));
				}
				Iterator it = myPages.iterator();
				while (it.hasNext()) {
					boolean matched = true;
					AdvicePageDefinition page =
						(AdvicePageDefinition) it.next();
					Iterator keys = reqs.keySet().iterator();
					while (matched && keys.hasNext()) {
						String key = keys.next().toString();
						String val = reqs.get(key).toString();
						if (!val.equals(page.get(key))) {
							matched = false;
						}
					}
					if (matched) {
						return "true";
					}
				}
				return "false";
			} else if (k.startsWith("page-list")) {
				StringBuffer retVal = new StringBuffer();
				StringTokenizer st = new StringTokenizer(k);
				st.nextToken();
				HashMap reqs = new HashMap();
				while (st.hasMoreTokens()) {
					String key = st.nextToken();
					String val = st.nextToken();
					reqs.put(
						URLDecoder.decode(key, "UTF-8"),
						URLDecoder.decode(val, "UTF-8"));
				}
				Iterator it = myPages.iterator();
				while (it.hasNext()) {
					boolean matched = true;
					AdvicePageDefinition page =
						(AdvicePageDefinition) it.next();
					Iterator keys = reqs.keySet().iterator();
					while (matched && keys.hasNext()) {
						String key = keys.next().toString();
						String val = reqs.get(key).toString();
						if (!val.equals(page.get(key))) {
							matched = false;
						}
					}
					if (matched) {
						retVal.append("<li>");
						retVal.append("[[[linkto agent://");
						retVal.append(NameEncoder.encode(get("agent-name")));
						retVal.append("/");
						retVal.append(
							NameEncoder.encode(page.get("this-name")));
						retVal.append("]]]</li>");
					}
				}
				if (retVal.length() > 0) {
					retVal.insert(0, "<ul>");
					retVal.append("</ul>");
				}
				return retVal.toString();
			} else {
				return super.get(k);
			}
		} catch (java.io.UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return "";
		}
	}

	/**
	 * A set of AdvicePageDefinition
	 */
	// here's where we deal with things that the agent "owns", like raters,
	// notebook pages and sections, and advice pages

	private Set myPages = new HashSet();
	/**
	 * A set of RaterDefinitions
	 */
	private Set myRaters = new HashSet();
	/**
	 * A set of NotebookPageDefinitions
	 */
	private Set myNotebookPages = new HashSet();
	/**
	 * A set of NotebookSectionDefinitions
	 */
	private Set myNotebookSections = new HashSet();
	/**
	 * 
	 */
	private String myPlainXML = "";
	/**
	 * This is where this agent would dump all of its generated HTML files and resources.
	 */
	private File outputFolder = null;

	/**
	 * Set the plain XML part of this agent.  This is raw agent format from the old agent structure
     * (stuff in manyminds.knowledgebase) that doesn't already have an Attributes type associated with it.
	 * @param s
	 */
	public void setPlainXML(String s) {
		myPlainXML = s;
	}

	/**
	 * 
	 * @return the raw XML rules to be parsed at runtime.
	 */
	public String getPlainXML() {
		return myPlainXML;
	}

	/**
	 * Add a rater
	 * @param rd
	 */
	public void addRater(RaterDefinition rd) {
		myRaters.add(rd);
	}

	/**
	 * Remove a rater
	 * @param rd
	 */
	public void removeRater(RaterDefinition rd) {
		myRaters.remove(rd);
	}

	/**
	 * Add an advice page
	 * @param p
	 */
	public void addPage(AdvicePageDefinition p) {
		myPages.add(p);
	}

	/**
	 * Remove an advice page
	 * @param p
	 */
	public void removePage(AdvicePageDefinition p) {
		myPages.remove(p);
	}

	/**
	 * Add a notebook page
	 * @param npd
	 */
	public void addNotebookPage(NotebookPageDefinition npd) {
		myNotebookPages.add(npd);
	}

	/**
	 * Remove a notebook page.
	 * @param npd
	 */
	public void removeNotebookPage(NotebookPageDefinition npd) {
		myNotebookPages.remove(npd);
	}

	/**
	 * Add a notebook section.
	 * @param nsd
	 */
	public void addNotebookSection(NotebookSectionDefinition nsd) {
		myNotebookSections.remove(nsd);
	}

	/**
	 * Remove a notebook Section.
	 * @param nsd
	 */
	public void removeNotebookSection(NotebookSectionDefinition nsd) {
		myNotebookSections.remove(nsd);
	}

	/**
	 * Get an advice page from this agent's list.
	 * @param s the name of the page (matching on this-name)
	 * @return the page, if it exists, null otherwise
	 */
	public AdvicePageDefinition getPage(String s) {
		Iterator it = myPages.iterator();
		while (it.hasNext()) {
			AdvicePageDefinition a = (AdvicePageDefinition) it.next();
			if (s.equals(a.get("this-name"))) {
				return a;
			}
		}
		return null;
	}

    /**
     * Get an rater  from this agent's list.
     * @param s the name of the rater (matching on this-name)
     * @return the rater, if it exists, null otherwise
     */
	public RaterDefinition getRater(String s) {
		Iterator it = myRaters.iterator();
		while (it.hasNext()) {
			RaterDefinition a = (RaterDefinition) it.next();
			if (s.equals(a.get("this-name"))) {
				return a;
			}
		}
		return null;
	}
    /**
     * Get an notebook page from this agent's list.
     * @param s the name of the page (matching on this-name)
     * @return the page, if it exists, null otherwise
     */
	public NotebookPageDefinition getNotebookPage(String s) {
		Iterator it = myNotebookPages.iterator();
		while (it.hasNext()) {
			NotebookPageDefinition a = (NotebookPageDefinition) it.next();
			if (s.equals(a.get("this-name"))) {
				return a;
			}
		}
		return null;
	}

    /**
     * Get a notebook section from this agent's list.
     * @param s the name of the section (matching on this-name)
     * @return the section, if it exists, null otherwise
     */
	public NotebookSectionDefinition getNotebookSection(String s) {
		Iterator it = myNotebookSections.iterator();
		while (it.hasNext()) {
			NotebookSectionDefinition a = (NotebookSectionDefinition) it.next();
			if (s.equals(a.get("this-name"))) {
				return a;
			}
		}
		return null;
	}

	/**
	 * Overridden to return the name of the agent
	 * @return this-name
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return get("this-name");
	}

	/**
	 * 
	 * @return an unmodifiable collection of all this agent's advice pages
	 */
	public Collection getPages() {
		return Collections.unmodifiableSet(myPages);
	}

	/**
	 * 
     * @return an unmodifiable collection of all this agent's raters
	 */
	public Collection getRaters() {
		return Collections.unmodifiableSet(myRaters);
	}

	/**
	 * 
     * @return an unmodifiable collection of all this agent's notebook pages
	 */
	public Collection getNotebookPages() {
		return Collections.unmodifiableSet(myNotebookPages);
	}

	/**
	 * 
     * @return an unmodifiable collection of all this agent's notebook sections
	 */
	public Collection getNotebookSections() {
		return Collections.unmodifiableSet(myNotebookSections);
	}

	/**
	 * Get the XML representation of this agent's structure.
	 * @return
	 * @see manyminds.agents.StaticAttributes#toXML()
	 */
	public String toXML() {
		StringBuffer retVal = new StringBuffer("<agent>\n");
		retVal.append(attributeXML());
        retVal.append("</agent>");
		return retVal.toString();
	}

	/**
	 * Generate all the appropriate HTML pages and spit them into a folder. Doesn't do 
     * anything anymore, because we've given up on static HTML, we'd rather generate these
     * pages on the fly.
	 * @param base the folder into which we should deposit the pages.
	 */
	public void outputPages(File base) {
		/*
		 * outputFolder = new File(base,get("this-name")); if
		 * (!outputFolder.exists()) { outputFolder.mkdirs(); } if
		 * (outputFolder.isDirectory()) { Iterator it = myPages.iterator();
		 * while (it.hasNext()) { AdvicePageDefinition ap =
		 * (AdvicePageDefinition)it.next(); String fileName =
		 * ap.get("page-filename"); if (fileName == null) { fileName =
		 * ap.nameYourself(outputFolder); } try { Writer os = new
		 * BufferedWriter(new FileWriter(new File(outputFolder,fileName)));
		 * os.write(ap.toHTML()); os.close(); } catch (Throwable t) {
		 * t.printStackTrace(); } } File raterOut = new
		 * File(outputFolder,"raters"); if (!raterOut.exists()) {
		 * raterOut.mkdirs(); } try { Writer os = new BufferedWriter(new
		 * FileWriter(new File(outputFolder,"agent.xml")));
		 * RaterDefinition.agentName = get("agent-name");
		 * RaterDefinition.LINKFRONT = "HTML/"; os.write(agentXMLHead()); it =
		 * myRaters.iterator(); while (it.hasNext()) { RaterDefinition rd =
		 * (RaterDefinition)it.next(); Writer ros = new BufferedWriter(new
		 * FileWriter(new File(raterOut,rd.getTitle()+".xml")));
		 * ros.write(rd.toRaterXML()); os.write(rd.toRuleXML()); ros.close(); }
		 * os.close(); } catch (Throwable t) { t.printStackTrace(); } }
		 * outputFolder = null;
		 */
	}

}