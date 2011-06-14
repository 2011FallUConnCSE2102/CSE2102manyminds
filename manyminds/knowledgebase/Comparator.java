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

public abstract class
Comparator
extends KnowledgeObject
implements KnowledgeListener, Precondition {
    
    protected Metaglobal myLeft;
    protected Metaglobal myRight;
    protected Boolean myComparison = FALSE;
    
    public
    Comparator() {
        super();
        setPolarity(true);
    }
    
    public
    Comparator(Metaglobal l, Metaglobal r) {
        super();
        myLeft = l;
        myRight = r;
        setPolarity(true);
    }
    
    public boolean
    isAppropriate(KnowledgeObject ko) {
        if (ko instanceof Metaglobal) {	
            return true;
        } else {
            return false;
        }
    }
    
        
    public void
    setLeft(Metaglobal l) {
        if (myLeft != null) {
            myLeft.removeKnowledgeListener(this);
        }
        myLeft = l;
        myLeft.addKnowledgeListener(this);
    }

    public void
    setRight(Metaglobal r) {
        if (myRight != null) {
            myRight.removeKnowledgeListener(this);
        }
        myRight = r;
        myRight.addKnowledgeListener(this);
   }
    
    public Object
    getValue() {
        return myComparison;
    }
    
    public void
    detailChanged(KnowledgeEvent ke) {}
    
    public void
    URLChanged(KnowledgeEvent ke) {}
    
    public void
    referenceChanged(KnowledgeEvent ke) {}
    
    public void
    referentChanged(KnowledgeEvent ke) {}
    
    public void
    valueChanged(KnowledgeEvent ke) {
        Boolean oldV = myComparison;
        if ((myLeft != null) && (myRight != null)) {
            if (!((myLeft.getValue() == Global.UNSET) || (myRight.getValue() == Global.UNSET))) {
                innerCompare();
                if (!oldV.equals(myComparison)) {                
                    fireValueChanged(oldV);
                }
            }
        }
    }

    public String
    toXML() {
        StringBuffer ret_val = new StringBuffer("<global-requirement> \n<comparator type=\"");
            try {
            if (this instanceof EqualsComparator) {
                ret_val.append("eq");
            } else if (this instanceof ContainsComparator) {
                ret_val.append("contains");
            } else if (this instanceof NotEqualsComparator) {
                ret_val.append("ne");
            } else if (this instanceof GreaterThanComparator) {
                ret_val.append("gt");
            } else if (this instanceof LessThanComparator) {
                ret_val.append("lt");
            }
            ret_val.append("\" />\n<metaglobal type=\"");
            if (myLeft instanceof ValueMetaglobal) {
                ret_val.append("value");
            } else if (myLeft instanceof ConstMetaglobal) {
                ret_val.append("const");
            } else if (myLeft instanceof LengthMetaglobal) {
                ret_val.append("length");
            } else if (myLeft instanceof TimestampMetaglobal) {
                ret_val.append("timestamp");
            }
            ret_val.append("\" side=\"left\">\n");
            if (myLeft instanceof ConstMetaglobal) {
                ret_val.append("<detail>"+myLeft.getDetail()+"</detail>");
            } else {
                ret_val.append("<reference>");
                ret_val.append(myLeft.getReference());
                ret_val.append("</reference>");
            }
            ret_val.append("\n</metaglobal>\n<metaglobal type=\"");
            if (myRight instanceof ValueMetaglobal) {
                ret_val.append("value");
            } else if (myRight instanceof ConstMetaglobal) {
                ret_val.append("const");
            } else if (myRight instanceof LengthMetaglobal) {
                ret_val.append("length");
            } else if (myRight instanceof TimestampMetaglobal) {
                ret_val.append("timestamp");
            }
            ret_val.append("\" side=\"right\">\n");
            if (myRight instanceof ConstMetaglobal) {
                ret_val.append("<detail>"+myRight.getDetail()+"</detail>");
            } else {
                ret_val.append("<reference>");
                ret_val.append(myRight.getReference());
                ret_val.append("</reference>");
            }
            ret_val.append("\n</metaglobal>\n</global-requirement>\n");
        } catch (Exception e) {
            ret_val.append("BROKEN");
        } finally {
            return ret_val.toString();
        }
    }
    protected abstract void
    innerCompare();
}