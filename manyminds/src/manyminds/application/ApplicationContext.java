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
 package manyminds.src.manyminds.application;
import java.awt.Component;
import java.net.URL;
import java.util.HashMap;

import javax.swing.JComponent;

import manyminds.debug.Logger;

/**
 * @author eric
 *
 */
public class
ApplicationContext {
    private HashMap myComponents = new HashMap();
    private URL myAgentBase = null;

    private static ApplicationContext single;
    private static Logger logger = Logger.getLogger("manyminds");

    private
    ApplicationContext() {
    }
    
    public static synchronized ApplicationContext
    getContext() {
        if (single == null) {
            single = new ApplicationContext();
        }
        return single;
    }

    public synchronized JComponent
    getApplicationComponent(String s) {
        return (JComponent)myComponents.get(s);
    }
    
    public synchronized Component
    addApplicationComponent(String s, JComponent c) {
        if (myComponents.containsKey(s)) {
            return (Component)myComponents.get(s);
        } else {
            myComponents.put(s,c);
            return c;
        }
    }
}