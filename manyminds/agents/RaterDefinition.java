package manyminds.agents;

import java.util.*;
import manyminds.datamodel.RaterModel;
import manyminds.datamodel.UpdatableListModel;
import manyminds.util.XMLEncoder;

public class
RaterDefinition 
extends AbstractAttributes
implements Attributes {

    public static String agentName = "[[[agent-name]]]";
    public static String LINKFRONT = "";
    
    public static class
    Advice {
        public String boxComment = "";
        public String overComment = "";
        public List linkList = new ArrayList();
        public List linkTextList = new ArrayList();

        public String
        toString() {
            return boxComment;
        }
        
        public String
        toXML() {
            if (linkList.size() > 1) {
                StringBuffer retVal = new StringBuffer();
                retVal.append("<multi-advice agent=\"");
                retVal.append(agentName);
                retVal.append("\">\n");
                retVal.append("<detail>");
                retVal.append(XMLEncoder.encode(overComment));
                retVal.append("</detail>\n");
                for (int i=0; i < linkList.size(); ++i) {
                    retVal.append("<give-advice>\n");
                    retVal.append("<url>");
                    retVal.append(XMLEncoder.encode(LINKFRONT+linkList.get(i)));
                    retVal.append("</url>\n");
                    retVal.append("<detail>");
                    retVal.append(XMLEncoder.encode(linkTextList.get(i).toString()));
                    retVal.append("</detail>\n</give-advice>\n");
                }
                retVal.append("</multi-advice>\n");
                return retVal.toString();
            } else if (linkList.size() == 1) {
                return XMLEncoder.encode(LINKFRONT+linkList.get(0).toString());
            } else {
                return "";
            }
        }
    }
    
    public static class
    Value {
        public String settingText = "";
        public String toolTipText = "";
        public Advice linkedAdvice = new Advice();
    }
    
    private String myTitle;
    private String myToolTip;
    private Advice myLinkTarget = new Advice();
    private List myValues = new LinkedList();

    public
    RaterDefinition() {
        super(false);
        myTitle = "New Rater";
        myToolTip = "";
    }
    
    public String
    get(String s) {
        return "";
    }
    
    public Collection
    getKeys() {
        return new LinkedList();
    }
    
    public
    RaterDefinition(RaterModel rm) {
        super(false);
        UpdatableListModel summaries = rm.getSummaries();
        UpdatableListModel toolTips = rm.getToolTips();
        myTitle = rm.getTitle();
        myToolTip = rm.getTitleToolTip();
        for (int i = 0; i < summaries.getSize(); ++i) {
            Value v = new Value();
            v.settingText = summaries.getElementAt(i).toString();
            v.toolTipText = toolTips.getElementAt(i).toString();
            v.linkedAdvice = new Advice();
            myValues.add(v);
        }
    }
    
    public void
    reset() {
        myTitle = "";
        myToolTip = "";
        myLinkTarget = null;
        myValues = new LinkedList();
    }
        
    public void
    setTitle(String s) {
        myTitle = s;
    }
    
    public List
    getValues() {
        return myValues;
    }
    
    public Advice
    getLinkTarget() {
        return myLinkTarget;
    }
    
    public void
    setLinkTarget(Advice a) {
        myLinkTarget = a;
    }
    
    public void
    setToolTip(String s) {
        myToolTip = s;
    }
    
    public String
    getTitle() {
        return myTitle;
    }
    
    public String
    getToolTip() {
        return myToolTip;
    }
    
    public void
    addValue(Value v) {
        myValues.add(v);
    }
    
    public Advice
    createAdvice() {
        return new Advice();
    }
    
    public Value
    createValue() {
        return new Value();
    }
    
    public void
    removeValue(int i) {
        myValues.remove(i);
    }

    public String
    toString() {
        return myTitle;
    }
    
    public void
    set(RaterDefinition rd) {
        myTitle = rd.myTitle;
        myToolTip = rd.myToolTip;
        myLinkTarget = rd.myLinkTarget;
        myValues = rd.myValues;
    }
    
    public String
    toXML() {
        StringBuffer retVal = new StringBuffer();
        retVal.append("<rater>\n");
        retVal.append("<attribute name=\"this.name\">");
        retVal.append(XMLEncoder.encode(myTitle));
        retVal.append("</attribute>\n");
        retVal.append("<attribute name=\"rater.tooltip\">");
        retVal.append(XMLEncoder.encode(myToolTip));
        retVal.append("</attribute>\n");
        retVal.append("<rater-linktarget>");
        retVal.append(myLinkTarget.toXML());
        retVal.append("</rater-linktarget>\n");
        retVal.append("<values>");
        Iterator it = myValues.iterator();
        while (it.hasNext()) {
            retVal.append("<value>\n");            
            Value v = (Value)it.next();
            retVal.append("<value-text>");
            retVal.append(XMLEncoder.encode(v.settingText));
            retVal.append("</value-text>\n");
            retVal.append("<value-tt>");
            retVal.append(XMLEncoder.encode(v.toolTipText));
            retVal.append("</value-tt>\n");
            if (v.linkedAdvice != null) {
                retVal.append("<value-linktarget>");
                retVal.append(v.linkedAdvice.toXML());
                retVal.append("</value-linktarget>\n");
                retVal.append("<value-boxcomment>");
                retVal.append(XMLEncoder.encode(v.linkedAdvice.boxComment));
                retVal.append("</value-boxcomment>\n");
            }
            retVal.append("</value>\n");            
        }
        retVal.append("</values>\n");
        retVal.append("</rater>\n");
        return retVal.toString();
    }

    public String
    toRuleXML() {
        StringBuffer retVal = new StringBuffer();
        Iterator it = myValues.iterator();
        int i = 0;
        while (it.hasNext()) {
            Value v = (Value)it.next();
            if ((v.linkedAdvice != null) && (v.linkedAdvice.toXML().trim().length() > 0)) {
                retVal.append("<rule type=\"normal\">\n<detail>Autogenerated rule for slider linked advice</detail>\n");
                retVal.append("    <global-requirement>\n        <comparator type=\"eq\" />\n");
                retVal.append("        <metaglobal type=\"value\" side=\"left\">\n");
                retVal.append("        <reference>");
                retVal.append(XMLEncoder.encode(myTitle));
                retVal.append("-group</reference>\n");
                retVal.append("        </metaglobal>\n        <metaglobal type=\"const\" side=\"right\">\n");
                retVal.append("            <detail>");
                retVal.append(i);
                retVal.append("</detail>\n");
                retVal.append("        </metaglobal>\n");
                retVal.append("    </global-requirement>\n");
                retVal.append("    <global-requirement>\n");
                retVal.append("        <comparator type=\"gt\" />\n        <metaglobal type=\"timestamp\" side=\"left\">\n");
                retVal.append("            <reference>");
                retVal.append(XMLEncoder.encode(myTitle));
                retVal.append("-group</reference>\n        </metaglobal>\n        <metaglobal type=\"const\" side=\"right\">\n");
                retVal.append("            <detail>5000</detail>\n        </metaglobal>\n    </global-requirement>\n");
                retVal.append("    <advice-give-action>\n        <url>");
                retVal.append(XMLEncoder.encode(v.linkedAdvice.toXML()));
                retVal.append("</url>\n");
                retVal.append("        <detail>");
                retVal.append(XMLEncoder.encode(v.linkedAdvice.boxComment));
                retVal.append("</detail>\n");
                retVal.append("    </advice-give-action>\n</rule>\n");
            }
            ++i;
        }
        retVal = resolveVariables(retVal);
        return retVal.toString();
    }
}