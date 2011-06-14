package manyminds.agents;

import java.util.Iterator;
import java.util.LinkedList;

public class
NotebookSectionDefinition 
extends StaticAttributes {
    
    private LinkedList myPages = new LinkedList();
    
    public
    NotebookSectionDefinition() {
        super(false);
    }
    
    public void
    addPage(String s) {
        myPages.add(s);
    }
                      
    public String
    toXML() {
        StringBuffer retVal = new StringBuffer("<notebook-section>\n");
        retVal.append(attributeXML());
        Iterator it = myPages.iterator();
        retVal.append("<pages>\n");
        while (it.hasNext()) {
            retVal.append("<page growable=\"false\">");
            retVal.append(it.next().toString());
            retVal.append("</page>\n");
        }
        retVal.append("</pages>\n");
        return retVal.toString();
    }

	/**
	 * @param string
	 */
	public void addNotebookPage(String string) {
		// TODO Auto-generated method stub
		
	}
}