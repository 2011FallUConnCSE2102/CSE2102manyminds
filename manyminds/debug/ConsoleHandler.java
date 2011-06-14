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

import java.io.IOException;

public class ConsoleHandler extends StreamHandler {

    private static final String LEVEL_PROPERTY = "manyminds.debug.ConsoleHandler.level";
    private static final String DEFAULT_LEVEL = Level.INFO.toString();
    private static final String FILTER_PROPERTY = "manyminds.debug.ConsoleHandler.filter";
    private static final String FORMATTER_PROPERTY = "manyminds.debug.ConsoleHandler.formatter";
    private static final String DEFAULT_FORMATTER = "manyminds.debug.SimpleFormatter";
    private static final String ENCODING_PROPERTY = "manyminds.debug.ConsoleHandler.encoding";

    public ConsoleHandler() {
        super( System.err,
               null,
               LEVEL_PROPERTY,
               DEFAULT_LEVEL,
               FILTER_PROPERTY,
               FORMATTER_PROPERTY,
               DEFAULT_FORMATTER,
               ENCODING_PROPERTY );
    }

    public void close() {
        try {
            assureHeadWritten();
            assureTailWritten();
            flush();
        }
        catch ( IOException ioe ) {
            reportError( "Problems closing.", ioe, ErrorManager.CLOSE_FAILURE );
        }
    }

    public void publish( LogRecord record ) {
        super.publish( record );
        flush();
    }

}