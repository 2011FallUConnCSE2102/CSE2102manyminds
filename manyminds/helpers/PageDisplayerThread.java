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
 package manyminds.helpers;

import java.applet.AppletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import manyminds.communication.KQMLMessage;
import manyminds.communication.MessageDispatcher;
import manyminds.debug.Level;
import manyminds.debug.Logger;
import manyminds.util.BrowserLauncher;

public class PageDisplayerThread extends MessageDispatcher {

    private AppletContext _context;
    
    private static PageDisplayerThread _single;
    private static Logger logger = Logger.getLogger("manyminds");
    
    synchronized public static void kickstart(AppletContext ac) {
        if (_single == null) {
            _single = new PageDisplayerThread(ac);
            //_single.start();
        }
    }
    
    private PageDisplayerThread(AppletContext ac) {
        super("page displayer");
        _context = ac;
    }

    public void handleMessages(List messages) {
        Iterator it = messages.iterator();
        while (it.hasNext()) {
            KQMLMessage mess = (KQMLMessage)it.next();
            logger.log(Level.FINE,"Attempting to display page "+mess.toString());
            if ((mess.getOntology().equals("control")) && (mess.getInReplyTo().equals("page to display"))) {
                displayPage(mess.getContent());
            } else {
                logger.log(Level.INFO,"Didn't know what to do with "+mess.toString());
            }
        }
    }
    
    public static void pageToDisplay(String newpage) {
        if (_single != null) {
            KQMLMessage.deliver("(tell :sender page displayer :receiver page displayer :ontology control :languague java :in-reply-to page to display :content "+newpage+")");
            _single.displayPage(newpage);
        } else {
            try {
                BrowserLauncher.openURL(newpage);
            } catch (Throwable t) {
                logger.log(Level.WARNING,"Unable to display "+newpage,t);
            }
        }
    }
    
    private void displayPage(String newpage) {
        try {
 //           displayPanel.setPage(new URL(newpage));
            _context.showDocument(new URL(newpage),"display");
            logger.log(Level.FINEST,"Displayed URL "+newpage);
        } catch (MalformedURLException e) {
            logger.log(Level.INFO,"Bad URL to display "+newpage,e);
        } catch (Throwable t) {
            logger.log(Level.INFO,"Error displaying "+newpage,t);
        }
    }
}