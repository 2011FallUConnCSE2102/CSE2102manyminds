package manyminds.agents;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class
NotebookPageDefinition 
extends StaticAttributes
implements Attributes {

    private List myRaters = new LinkedList();
    private List myWorkspaces = new LinkedList();
    
    public static class
    WorkspaceDefinition {
        private String myTitle, myTooltip, myURL, myData, mySize;

        public
        WorkspaceDefinition(String txt, String tt, String url, String id, String size) {
            myTitle = txt;
            myTooltip = tt;
            myURL = url;
            myData = id;
            mySize = size;
        }
    
        public String
        toXML() {
            StringBuffer retVal = new StringBuffer("<workspace>\n");
            retVal.append("<text><![CDATA[");
            retVal.append(myTitle);
            retVal.append("]]></text>\n");
            retVal.append("<tooltip><![CDATA[");
            retVal.append(myTooltip);
            retVal.append("]]></tooltip>\n");
            retVal.append("<url><![CDATA[");
            retVal.append(myURL);
            retVal.append("]]></url>\n");
            retVal.append("<id><![CDATA[");
            retVal.append(myData);
            retVal.append("]]></id>\n");
            retVal.append("<size><![CDATA[");
            retVal.append(mySize);
            retVal.append("]]></size>\n</workspace>\n");
            return retVal.toString();
        }
    }
            
    
    public
    NotebookPageDefinition() {
        super(false);
    }
    
    public String
    get(String s) {
        return "";
    }
    
    public Collection
    getKeys() {
        return new LinkedList();
    }
    
    public void
    addRater(String s) {
        myRaters.add(s);
    }
    
    public void
    addWorkspace(String txt, String tt, String url, String id, String size) {
        myWorkspaces.add(new WorkspaceDefinition(txt,tt,url,id,size));
    }
    
    public void
    addWorkspace(WorkspaceDefinition wd) {
        myWorkspaces.add(wd);
    }
    
    public String
    toXML() {
        StringBuffer retVal = new StringBuffer("<notebook-page>\n");
        retVal.append(attributeXML());
        retVal.append("<raters>\n");
        Iterator it = myRaters.iterator();
        while (it.hasNext()) {
            retVal.append("<raters>");
            retVal.append(it.next().toString());
            retVal.append("</rater>\n");
        }
        it = myWorkspaces.iterator();
        while (it.hasNext()) {
            retVal.append(((WorkspaceDefinition)it.next()).toXML());
        }
        retVal.append("</notebook-page>\n");
        return retVal.toString();
    }
    
}