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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketHandler extends StreamHandler {

    private static final String LEVEL_PROPERTY = "manyminds.debug.SocketHandler.level";
    private static final String DEFAULT_LEVEL = Level.INFO.toString();
    private static final String FILTER_PROPERTY = "manyminds.debug.SocketHandler.filter";
    private static final String FORMATTER_PROPERTY = "manyminds.debug.SocketHandler.formatter";
    private static final String DEFAULT_FORMATTER = "manyminds.debug.SimpleFormatter";
    private static final String ENCODING_PROPERTY = "manyminds.debug.SocketHandler.encoding";

    private static final String HOST_PROPERTY = "manyminds.debug.SocketHandler.host";
    private static final String PORT_PROPERTY = "manyminds.debug.SocketHandler.port";

    private Socket theSocket;
    private BufferedOutputStream buffered;

    public SocketHandler() throws IOException {
        super( null,
               null,
               LEVEL_PROPERTY,
               DEFAULT_LEVEL,
               FILTER_PROPERTY,
               FORMATTER_PROPERTY,
               DEFAULT_FORMATTER,
               ENCODING_PROPERTY );
        LogManager manager = LogManager.getLogManager();
        String host = manager.getProperty( HOST_PROPERTY );
        host.length();
        int port = 0;
        try {
            port = Integer.parseInt( manager.getProperty( PORT_PROPERTY ) );
        }
        catch ( NumberFormatException nfe ) {
            throw new IllegalArgumentException( "Couldn't parse port property!" );
        }
        if ( port == 0 ) {
            throw new IllegalArgumentException( "Can't connect to port zero!" );
        }
        init( host, port );
    }

    public SocketHandler( String aHost, int aPort ) throws IOException {
        super( null,
               null,
               LEVEL_PROPERTY,
               DEFAULT_LEVEL,
               FILTER_PROPERTY,
               FORMATTER_PROPERTY,
               DEFAULT_FORMATTER,
               ENCODING_PROPERTY );
        init( aHost, aPort );
    }

    private void init( String aHost, int aPort ) throws IOException {
        theSocket = new Socket( aHost, aPort );
        OutputStream out = theSocket.getOutputStream();
        theSocket.getInputStream().close();
        buffered = new BufferedOutputStream( out );
        setOutputStream( buffered );
    }

    public void close() {
        super.close();
        if ( theSocket != null ) {
            try {
                theSocket.close();
            }
            catch ( IOException ioe ) {
                reportError( "Unable to close!", ioe, ErrorManager.CLOSE_FAILURE );
            }
        }
    }

    public synchronized void publish( LogRecord record ) {
        super.publish( record );
        try {
            buffered.flush();
        }
        catch ( IOException ioe ) {
            reportError( "Unable to flush buffered output stream", ioe, ErrorManager.FLUSH_FAILURE );
        }
    }
}