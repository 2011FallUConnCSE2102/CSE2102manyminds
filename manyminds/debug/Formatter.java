// Java Logging API for JDKs prior to 1.4
// Copyright (C) 2001 Brian R. Gilstrap
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details (a copy of the license
// .1 is included in the file GLPL in the doc directory of the source
// distribution).
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
 package manyminds.debug;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public abstract class Formatter {

    protected Formatter() {
    }

    public abstract String format( LogRecord record );

    public String formatMessage( LogRecord record ) {
        String key = record.getMessage();
        if ( key == null ) {
            return null;
        }
        String formatString = null;
        ResourceBundle bundle = record.getResourceBundle();
        if ( bundle == null ) {
            String bundleName = record.getResourceBundleName();
            if ( bundleName != null ) {
                bundle = ResourceBundle.getBundle( bundleName );
            }
        }
        if ( bundle != null ) {
            try {
                formatString = bundle.getString( key );
            }
            catch ( MissingResourceException mre ) {
            }
            if ( formatString == null ) {
                formatString = key;
            }
        }
        else {
            formatString = key;
        }
        Object[] parameters = record.getParameters();
        if ( parameters == null || parameters.length == 0 ) {
            return formatString;
        }
        if ( formatString.indexOf( "{0" ) >= 0 ) {
            return MessageFormat.format( formatString, parameters );
        }
        else {
            return formatString;
        }
    }

    public String getHead( Handler h ) {
        return "";
    }

    public String getTail( Handler h ) {
        return "";
    }

}
