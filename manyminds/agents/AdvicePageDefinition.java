package manyminds.agents;

import java.io.File;
import java.util.Iterator;

import manyminds.debug.Level;

/**
 * 
 * Used to store the definition of an advice page.
 * 
 * @author eric
 *
 */
public class AdvicePageDefinition
	extends StaticAttributes
	implements MutableAttributes {

	/**
	 * This Attributes will not be added to the global list.
	 */
	public AdvicePageDefinition() {
		super(false);
	}

	/**
     * Creates this as a copy of another attributes, taking all its parents and key-value pairs.
	 * @param a the Attributes to clone.
	 */
	public AdvicePageDefinition(Attributes a) {
		super(false);
		Iterator it = a.getKeys().iterator();
		while (it.hasNext()) {
			String key = it.next().toString();
			if (!key.startsWith("this")) {
				set(key, a.get(key));
			}
		}
		it = a.getParents().iterator();
		while (it.hasNext()) {
			addParent(it.next().toString());
		}
	}

	/**
     * Finds an unused filename with a name that looks like this-name (with stripped whitespace and special chars)
     * and sets the parameter "page-filename" to that filename.
	 * @param f the folder to look into.
	 * @return the filename we found.
	 */
	public String nameYourself(File f) {
		StringBuffer newName = new StringBuffer(get("this-name").toLowerCase());
		for (int i = newName.length() - 1; i >= 0; --i) {
			char c = newName.charAt(i);
			if (!Character.isLetterOrDigit(c)) {
				newName.deleteCharAt(i);
			}
		}
		if (newName.length() >= 24) {
			newName.setLength(24);
		}
		File check = new File(f, newName.toString() + ".html");
		int i = 0;
		while (check.exists()) {
			check = new File(f, newName.toString() + i + ".html");
			++i;
		}
		set("page-filename", check.getName());
		return check.getName();
	}

	/**
     * Turns this advice page into html by first applying the layouts to the page (which usually 
     * add headers and footers to the body-text value and then calling resolveVariables on the result.
	 * @return an HTML string with all variables resolved.
	 */
	public String toHTML() {
		Layout myLayout = Layout.getLayout(get("page-layout"));
		StringBuffer layout = myLayout.layoutPage(this);
		StringBuffer resolvedLayout = null;
		try {
			resolvedLayout = resolveVariables(layout);
		} catch (Throwable t) {
			logger.log(
				Level.SEVERE,
				"Error resolving variables in " + layout.toString(),
				t);
		}
		return resolvedLayout.toString();
	}

	public String toString() {
		return get("this-name");
	}

//	public String toXML() {
//		StringBuffer retVal = new StringBuffer("<page>\n");
//		Iterator it = getParents().iterator();
//		while (it.hasNext()) {
//			retVal.append("<parent><![CDATA[");
//			retVal.append((String) it.next());
//			retVal.append("]]></parent>\n");
//		}
//		it = myAttributes.keySet().iterator();
//		while (it.hasNext()) {
//			String k = (String) it.next();
//			String v = (String) myAttributes.get(k);
//			retVal.append("<attribute name=\"");
//			retVal.append(k);
//			retVal.append("\"><![CDATA[");
//			retVal.append(v);
//			retVal.append("]]></attribute>\n");
//		}
//		retVal.append("</page>\n");
//		return retVal.toString();
//	}
}
