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
import manyminds.debug.Level;

public class
EqualsComparator
extends Comparator {
    
    public
    EqualsComparator() {
        super();
    }
    
    public
    EqualsComparator(Metaglobal l, Metaglobal r) {
        super(l,r);
    }
    
    public String
    getDetail() {
        if ((myLeft != null) && (myRight != null)) {
            return myLeft.getDetail()+" must be equal to "+myRight.getDetail();
        } else {
            return "Equal comparator";
        }
    }
    
    protected void
    innerCompare() {
        if ((myLeft != null) && (myRight != null)) {
            if (myLeft instanceof ValueMetaglobal) {
                if (myRight instanceof ValueMetaglobal) {
                    if (myLeft.getValue().equals(myRight.getValue())) {
                        myComparison = TRUE;
                    } else {
                        myComparison = FALSE;
                    }
                } else if (myRight instanceof ConstMetaglobal) {
                    if (myLeft.getValue().equals(myRight.getValue())) {
                        myComparison = TRUE;
                    } else {
                        myComparison = FALSE;
                    }
                } else {
                    myComparison = FALSE;
                }
            } else if (myLeft instanceof LengthMetaglobal) {
                if (myRight instanceof ConstMetaglobal) {
                    try {
                        int lInt = ((Integer)myLeft.getValue()).intValue();
                        int rInt = ((new Integer(myRight.getValue().toString()))).intValue();
                        if (lInt == rInt) {
                            myComparison = TRUE;
                        } else {
                            myComparison = FALSE;
                        }
                    } catch (NumberFormatException nfe) {
                        myComparison = FALSE;
                        logger.log(Level.INFO,"Not a number in equals comparator",nfe);
                    }
                } else if (myRight instanceof LengthMetaglobal) {
                    int lInt = ((Integer)myLeft.getValue()).intValue();
                    int rInt = ((Integer)myRight.getValue()).intValue();
                    if (lInt == rInt) {
                        myComparison = TRUE;
                    } else {
                        myComparison = FALSE;
                    }
                } else {
                    myComparison = FALSE;
                }
            } else if (myLeft instanceof TimestampMetaglobal) {
                myComparison = FALSE;
            } else if (myLeft instanceof ConstMetaglobal) {
                myComparison = FALSE; //don't handle left hand consts yet, this should be fixed!
            } else {
                myComparison = FALSE; //don't know what sort of metaglobal this is
            }
        } else {
            myComparison = FALSE; //one or both comparees are null
        }
    }    
}