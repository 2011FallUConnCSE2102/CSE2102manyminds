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
LessThanComparator
extends Comparator {
    
    public
    LessThanComparator() {
        super();
    }
    
    public
    LessThanComparator(Metaglobal l, Metaglobal r) {
        super(l,r);
    }
    
    public String
    getDetail() {
        if ((myLeft != null) && (myRight != null)) {
            return myLeft.getDetail()+" must be less than "+myRight.getDetail();
        } else {
            return "Less than comparator";
        }
    }
    
    protected void
    innerCompare() {
        if ((myLeft != null) && (myRight != null)) {
            if (myLeft instanceof ValueMetaglobal) {
                if (myRight instanceof ConstMetaglobal) {
                    try {
                        int lInt = ((new Integer(myLeft.getValue().toString()))).intValue();
                        int rInt = ((new Integer(myRight.getValue().toString()))).intValue();
                        if (lInt < rInt) {
                            myComparison = TRUE;
                        } else {
                            myComparison = FALSE;
                        }
                    } catch (NumberFormatException nfe) {
                        myComparison = FALSE;
                    }
                } else if (myRight instanceof LengthMetaglobal) {
                    try {
                        int lInt = ((new Integer(myLeft.getValue().toString()))).intValue();
                        int rInt = ((Integer)myRight.getValue()).intValue();
                        if (lInt < rInt) {
                            myComparison = TRUE;
                        } else {
                            myComparison = FALSE;
                        }
                    } catch (NumberFormatException nfe) {
                        myComparison = FALSE;
                    }
                } else if (myRight instanceof ValueMetaglobal) {
                    try {
                        int lInt = ((new Integer(myLeft.getValue().toString()))).intValue();
                        int rInt = ((new Integer(myRight.getValue().toString()))).intValue();
                        if (lInt < rInt) {
                            myComparison = TRUE;
                        } else {
                            myComparison = FALSE;
                        }
                    } catch (NumberFormatException nfe) {
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
                        if (lInt < rInt) {
                            myComparison = TRUE;
                        } else {
                            myComparison = FALSE;
                        }
                    } catch (NumberFormatException nfe) {
                        logger.log(Level.WARNING,"Number format exception in less than comparison "+myRight.getValue().toString(),nfe);
                        myComparison = FALSE;
                    }
                } else if (myRight instanceof LengthMetaglobal) {
                    int lInt = ((Integer)myLeft.getValue()).intValue();
                    int rInt = ((Integer)myRight.getValue()).intValue();
                    if (lInt < rInt) {
                        myComparison = TRUE;
                    } else {
                        myComparison = FALSE;
                    }
                } else {
                    myComparison = FALSE;
                }
            } else if (myLeft instanceof TimestampMetaglobal) {
                if (myRight instanceof TimestampMetaglobal) {
                    if (((Long)myLeft.getValue()).compareTo((Long) myRight.getValue()) < 0) {
                        myComparison = TRUE;
                    } else {
                        myComparison = FALSE;
                    }
                } else if (myRight instanceof ConstMetaglobal) {
                    try {
                        Long rLong = new Long(myRight.getValue().toString());
                        if (((Long)myLeft.getValue()).compareTo(rLong) < 0) {
                            myComparison = TRUE;
                        } else {
                            myComparison = FALSE;
                        }
                    } catch (NumberFormatException nfe) {
                        myComparison = FALSE;
                    }
                } else {
                    myComparison = FALSE;
                }
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