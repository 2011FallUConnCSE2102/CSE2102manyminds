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

package manyminds.util.browser;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.apple.eawt.CocoaComponent;

public class
WebKitComponent
extends CocoaComponent {

    static {
        try {
            System.loadLibrary("WebKitComponent");
        }
        catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    public static final int storeJavaObject = 1;
    public static final int loadURL = 2;
    public static final int goBack = 3;
    public static final int goForward = 4;
    public static final int reloadPage = 5;
	    
    public native int createNSView();

    public WebKitComponent() {
    	super();
    }
    
    public void
    loadURL(final String url) {
        (new Thread() {
            public void
            run() {
                sendMessage(loadURL,url);
            }
        }).start();
    }
    
    public void
    goBack() {
        (new Thread() {
            public void
            run() {
                sendMessage(goBack,"");
            }
        }).start();
    }
    
    public void
    reloadPage() {
        (new Thread() {
            public void
            run() {
                sendMessage(reloadPage,"");
            }
        }).start();
    }
    
    public void
    goForward() {
        (new Thread() {
            public void
            run() {
                sendMessage(goForward,"");
            }
        }).start();
    }
    
    public Dimension getMaximumSize() {
        return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
    }
    
    public Dimension getMinimumSize() {
        return new Dimension(160, 90);
    }
    
    public Dimension getPreferredSize() {
        return manyminds.ManyMindsConstants.FULL_WIDTH_FULL_HEIGHT;
    }
    
    public void addNotify(){
        super.addNotify();
        sendMessage(WebKitComponent.storeJavaObject,this);
        addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                    requestFocusInWindow();
                }
    	});
    }
}
