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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

public class XMLFormatter extends Formatter {

    private static final String HEADER1 = "<?xml version=\"1.0\" encoding=\"";
    private static final String HEADER2 = "\" standalone=\"no\"?>\n<!DOCTYPE log SYSTEM \"logger.dtd\">\n<log>\n";
    private static final String TAIL = "</log>\n";

    private DateFormat dateFormatter;

    public XMLFormatter() {
        dateFormatter = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
    }

    public String format( LogRecord record ) {
        StringBuffer formatted = new StringBuffer( "<record>\n  <date>" );
        formatted.append( dateFormatter.format( new Date( ) ) );
        formatted.append( "</date>\n  <millis>" );
        formatted.append( record.getMillis() );
        formatted.append( "</millis>\n  <sequence>" );
        formatted.append( record.getSequenceNumber() );
        formatted.append( "</sequence>\n  " );
        String loggerName = record.getLoggerName();
        if ( loggerName != null ) {
            formatted.append( "<logger>" );
            formatted.append( loggerName );
            formatted.append( "</logger>\n  " );
        }
        formatted.append( "<level>" );
        formatted.append( record.getLevel() );
        formatted.append( "</level>\n  " );
        String className = record.getSourceClassName();
        if ( className != null ) {
            formatted.append( "<class>" );
            formatted.append( className );
            formatted.append( "</class>\n  " );
        }
        String methodName = record.getSourceMethodName();
        if ( methodName != null ) {
            formatted.append( "<method>" );
            formatted.append( methodName );
            formatted.append( "</method>\n  " );
        }
        // NOPNCONFORMANT? - we provide the thread ID unless it is zero
        int threadID = record.getThreadID();
        if ( threadID != 0 ) {
            formatted.append( "<thread>" );
            formatted.append( threadID );
            formatted.append( "</thread>\n  " );
        }
        String rawMessage  = record.getMessage();
        boolean localizedTheMessage = false;
        String localizedMessage = rawMessage;
        ResourceBundle rb = record.getResourceBundle();
        if ( rb != null ) {
            String temp = rb.getString( rawMessage );
            if ( temp != null ) {
                localizedMessage = temp;
                localizedTheMessage = true;
            }
        }
        //CHANGED to add CDATA block
        formatted.append( "<message><![CDATA[" );
        formatted.append( localizedMessage );
        //CHANGED to terminate CDATA block
        formatted.append( "]]></message>" );
        if ( localizedTheMessage ) {
            formatted.append( "<key>" );
            formatted.append( rawMessage );
            formatted.append( "</key>\n  " );
            formatted.append( "<catalog>" );
            formatted.append( record.getResourceBundleName() );
            formatted.append( "</catalog>\n  " );
        }
        // NONCONFORMANT? We always provide the params, even if the
        // message was not localized
        Object[] params = record.getParameters();
        if ( params != null && params.length > 0 ) {
            formatParams( formatted, params );
        }
        Throwable thrown = record.getThrown();

        if ( thrown != null ) {
            formatException( formatted, thrown );
        }
        formatted.append( "\n</record>\n" );

        return formatted.toString();
    }

    private void formatException( StringBuffer formatted, Throwable thrown ) {
        formatted.append( "\n  <exception>" );
        String message = thrown.getMessage();
        if ( message != null ) {
            //CHANGED to add CDATA block
            formatted.append( "\n      <message><![CDATA[" ); 
            formatted.append( message );
            //CHANGED to terminate CDATA block
            formatted.append( "]]></message>" );
        }
        formatted.append("\n      <type><![CDATA[");
        formatted.append(thrown.getClass().toString());
        formatted.append("]]></type>");
        // Go through the stack trace one line at a time and convert the info
        StringWriter sw = new StringWriter();
        thrown.printStackTrace( new PrintWriter( sw ) );
        String thrownBacktrace = sw.getBuffer().toString();
        StringTokenizer lines = new StringTokenizer( thrownBacktrace, "\n\r" );
        if ( ! lines.hasMoreTokens() ) {
            formatted.append( "\nERROR PARSING THROWABLE BACKTRACE" );
        }
        else {
            String firstLine = lines.nextToken();
            while ( lines.hasMoreTokens() ) {
                try {
                    String line = lines.nextToken();
                    // Rip off the 'at '
                    line = line.substring( "at ".length() );
                    line = line.trim();
                    StringTokenizer t = new StringTokenizer( line, "()" );
                    String classAndMethodName = t.nextToken();
                    int lastPeriod = classAndMethodName.lastIndexOf( '.' );
                    String className = classAndMethodName.substring( 0, lastPeriod );
                    String methodName = classAndMethodName.substring( lastPeriod + 1 );
                    String methodandlineNumber = t.nextToken();
					int lastColon = methodandlineNumber.lastIndexOf(':');
                    String lineNumber = methodandlineNumber.substring(lastColon + 1);
                    formatted.append( "\n    <frame>" );
                    formatted.append( "\n      <class><![CDATA[" );
                    formatted.append( className );
                    formatted.append( "]]></class>" );
					formatted.append( "\n      <method><![CDATA[" );
                    formatted.append( methodName );
                    formatted.append( "]]></method>" );
					formatted.append( "\n      <line>" );
                    formatted.append( lineNumber );
                    formatted.append( "</line>" );
                    formatted.append( "\n    </frame>" );
                }
                catch ( Exception e ) {
                    formatted.append( "\nERROR PARSING THROWABLE BACKTRACE" );
                }
            }
        }
        formatted.append( "\n  </exception>" );
    }

    private void formatParams( StringBuffer formatted, Object[] params ) {
        for ( int index = 0 ; index < params.length ; index++ ) {
            try {
                Object param = params[ index ];
                //CHANGED to add CDATA block
                formatted.append( "<param><![CDATA[" );
                if ( param != null ) {
                    formatted.append( param.toString() );
                }
                //CHANGED to terminate CDATA block
                formatted.append( "]]></param>\n  " );
            }
            catch ( Exception e ) {
                // Ignore
            }
        }
    }

    public String getHead( Handler h ) {
        return HEADER1 + h.getEncoding() + HEADER2;
    }

    public String getTail( Handler h ) {
        return TAIL;
    }
}
