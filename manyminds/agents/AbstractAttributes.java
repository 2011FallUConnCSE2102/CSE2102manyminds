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

import java.util.*;

import manyminds.debug.*;

/**
 * Handles all the useful and generic Attributes activity that can get farmed out to this level.  Specifically
 * it deals with resolving inheritance and variable resolution issues.  Most Attributes objects should
 * subclass AbstractAttributes rather than directly implement Attributes.
 * 
 * @author Eric Eslinger
 *
 */
public abstract class AbstractAttributes implements Attributes {

	/**
     * The list of parents this Attributes object has.  Used at variable resolution time if we find an unbound variable.
	 * Comment for <code>myParents</code>
	 */
	private List myParents = new LinkedList();
	/**
     * The textual marker for the beginning of a variable.
	 * Comment for <code>VARIABLE_BEGIN</code>
	 */
	private static final String VARIABLE_BEGIN = "[[[";
	/**
     * The textual marker for the end of a variable.
	 * Comment for <code>VARIABLE_END</code>
	 */
	private static final String VARIABLE_END = "]]]";
	/**
     * This is a list of all the AbstractAttributes that we've created.  It is useful so you can get an Attributes object by name if you need to.
	 * Comment for <code>allAttributes</code>
	 */
	private static Set allAttributes =
		Collections.synchronizedSet(new HashSet());
	protected static Logger logger = Logger.getLogger("manyminds.editor");

	/**
     * You can't make one, just instantiate a subclass of one.
	 * @param global True if you want this attributes object to be added to the global attributes list.
	 */
	protected AbstractAttributes(boolean global) {
		if (global) {
			allAttributes.add(this);
		}
	}

	/** 
     * Get the list of parents this Attributes has.
	 * @see manyminds.agents.Attributes#getParents()
     * @return a List of Attribute names (not Attributes, just strings)
	 */
	public List getParents() {
		return myParents;
	}

	/**
     * Override equal to be better.
	 * @see java.lang.Object#equals(java.lang.Object)
     * @return if o is an Attributes whose "this-name" key has the same value as this one.
	 */
	public boolean equals(Object o) {
		if (o instanceof Attributes) {
			String s = get("this-name");
			if ((s != null) && (s.equals(((Attributes) o).get("this-name")))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return A set of all global attributes.  It is unmodifiable.
	 */
	public static Set getAllAttributes() {
		return Collections.unmodifiableSet(allAttributes);
	}

	/**
	 * @param n The name of the attributes object to look up.
	 * @return The Attributes whose "this-name" key has value n.
	 */
	public static Attributes getAttributes(String n) {
		Iterator it = allAttributes.iterator();
		while (it.hasNext()) {
			Attributes a = (Attributes) it.next();
			if (n.equals(a.get("this-name"))) {
				return a;
			}
		}
		return null;
	}

	/**
     * Sometimes an Attributes goes away.  More likely inside the agent editor piece rather than the core viewer.
	 * @param a The Attributes object to be removed from the global list.  
	 */
	public static void removeAttributes(Attributes a) {
		allAttributes.remove(a);
	}

	/**
     * Tries to get a value from the parents to this object.  Keys that start with "this-" don't get
     * dynamically bound up the inheritance tree (because that would make naming using this-name act funny).  The
     * search starts with the first parent in the list, and is dependent on the parent's definition of get(), but
     * usually will go all the way up an inheritance path before going on to the next element in the 
     * parent list.
     * 
	 * @param s the key to look up
	 * @return the first encountered value of the key.
	 */
	protected String getFromParents(String s) {
		if (!s.startsWith("this-")) {
			Iterator it = myParents.iterator();
			while (it.hasNext()) {
				String parent = (String) it.next();
				try {
					Object o = getAttributes(parent).get(s);
					if (o != null) {
						return (String) o;
					}
				} catch (NullPointerException npe) {
					System.err.println("NPE looking for " + parent);
				}
			}
		}
		return null;
	}

	/**
     * Adds a parent to the end of the list.
	 * @see manyminds.agents.Attributes#addParent(java.lang.String)
	 * @param p the name of the parent (not an attributes object)
     */
	public void addParent(String p) {
		if (myParents.indexOf(p) < 0) {
			myParents.add(p);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 * @return the value of the "this-name" key
     */
	public String toString() {
		return get("this-name");
	}

	/**
     * Inserts a parent into the list.  The parent list order is important because getFromParents
     * will return when it finds the first match, and searches the list in order.  So if the same key
     * is defined in multiple parents, only the first value is returned.
	 * @see manyminds.agents.Attributes#addParent(java.lang.String, int)
     * @param p the name of the Attributes to add
     * @param i the position to insert into
	 */
	public void addParent(String p, int i) {
		if (myParents.indexOf(p) < 0) {
			if ((i >= 0) && (i < myParents.size())) {
				myParents.add(i, p);
			} else {
				myParents.add(p);
			}
		}
	}

	/**
     * This is the really interesting part of the attributes stuff.  You create a stringbuffer that contains
     * variables and pass it in, and those variables are looked up and replaced with their values. This is a 
     * multipass resolver, where on each pass the innermost variable is resolved.  If it can't be resolved,
     * the open and close braces are stripped from the working value.  So:<p>
     * <code>[[[ [[[indirect-name]]] ]]]</code><p>
     * Would first resolve the indirect-name key and replace it with a value, which would be looked up on the 
     * second pass. At the third pass, no variables are found, so it returns all the way up the parse tree with
     * the value of whatever was found using the value of indirect-name as a key.
     * 
	 * @see manyminds.agents.Attributes#resolveVariables(java.lang.StringBuffer)
	 * @param a StringBuffer with something worth resolving.
     * @return a StringBuffer with all possible variables resolved.  This isn't going to be the passed in
     * value.
     */
	public StringBuffer resolveVariables(StringBuffer sb) {
		StringBuffer retVal = new StringBuffer();
		String workingString = sb.toString();
		int braceEnd = workingString.indexOf(VARIABLE_END);
		int braceBegin = 0;
		int lastBraceEnd = 0;

		boolean didReplace = false;
		while (braceEnd > -1) {
			int newBraceBegin =
				workingString.lastIndexOf(VARIABLE_BEGIN, braceEnd);
			if (newBraceBegin <= braceBegin) {
				braceEnd =
					workingString.indexOf(
						VARIABLE_END,
						braceEnd + VARIABLE_END.length());
			} else if (newBraceBegin > -1) {
				braceBegin = newBraceBegin;
				retVal.append(
					workingString.substring(lastBraceEnd, braceBegin));
				String varName =
					workingString.substring(
						braceBegin + VARIABLE_BEGIN.length(),
						braceEnd);
				try {
					String varVal = get(varName);
					if (varVal != null) {
						retVal.append(varVal);
						didReplace = true;
					} else {
						///retVal.append(VARIABLE_BEGIN);
						retVal.append(varName);
                        didReplace = true;
						///retVal.append(VARIABLE_END);
					}
				} catch (Throwable t) {
					logger.log(Level.SEVERE, "Error handling " + varName, t);
					throw new Error();
				}
				lastBraceEnd = braceEnd + VARIABLE_END.length();
				braceEnd = workingString.indexOf(VARIABLE_END, lastBraceEnd);
			} else {
				braceEnd = workingString.indexOf(VARIABLE_END, lastBraceEnd);
			}
		}
		retVal.append(workingString.substring(lastBraceEnd));
		if (didReplace) {
			return resolveVariables(retVal);
		} else {
			return retVal;
		}
	}


}