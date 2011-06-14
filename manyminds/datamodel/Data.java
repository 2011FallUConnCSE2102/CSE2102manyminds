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
 package manyminds.datamodel;
/**
 * @author eric
 *
 */
/**
 * Anything that implements the Data interface represents something that the
 * ManyMinds system needs to keep track of.  This includes simple text snippets 
 * (the contents of a workspace or text box) and the value of a Self Assessment
 * Slider.  
 * 
 * The Data objects currently use a complicated peering system (baroque, actually) to 
 * synchronize linked data objects in different DataServers.  Using this model, it is 
 * possible to connect to a DataServer on a remote computer but still share data and
 * have those Data objects stay synchronized (say) when two people are concurrently
 * modifying the same object.
 * 
 * In the future, this whole package (and the Data object in particular) needs to be
 * replaced with an actual database that is good at storing whatever we need in a more
 * distributed fashion.  Have a look at the way the ManyMinds Analyzer stores data with the
 * checkin/checkout model rather than immediate synchronization.
 * 
 * @author eric
 *
 */
public interface
Data {
    
    public static final int DOCUMENT = 1;
    public static final int RSA = 2;
    public static final int GROUP = 3;
    public static final int TABLE = 4;
    
    /**
     * Set the value of this Data object to some string.  This will usually entail
     * parsing the string and turning it into the internal representation (e.g. the RaterModel
     * actually stores it's value as an Integer.
     * 
     * @param s the new value.
     */
    public void
    setValue(String s);
    

    /**
     * Formats the internal value into a string and returns it.  Note that AbstractData's equals() method
     * compares the getValue() of both objects.  So if two Data objects should be equal, their getValue() calls
     * should return Strings that are equals().
     * @return The formatted value
     */
    public String
    getValue();
    
    /**
     * What kind of Data object is this representing?  The lower level database stuff requires an enum for this.
     * @return
     */
    public int
    getType();
    
    
    /**
     * The TypeString of a data object is a nonvolatile piece of information (it doesn't change the way
     * the data's value does) that represents the structure of this data object.  In particular, the RaterModel
     * uses the TypeString field to store the XML definition of the Rater's structure (tooltip names, etc)
     * @return TypeString
     */
    public String
    getTypeString();
    
    
    /**
     * Look, a setter method for the TypeString.  See getTypeString();
     * @param s
     */
    
    public void
    setTypeString(String s);
    
    /**
     * Resets the Data to it's default value (0, "", whatever is appropriate for that Data implementation)
     * @return true if it works, false otherwise
     */
    
    public boolean
    reset();
    
    /**
     * Adds a data listener to this local Data object. Note that data listeners are local, but may end up triggered if
     * this data changes because of an upstream or downstream event.
     * @param pdl
     */
    public void
    addDataListener(DataListener pdl);
    
    /**
     * Removes a data listener from this Data object
     * @param pdl
     */
    public void
    removeDataListener(DataListener pdl);
    
}