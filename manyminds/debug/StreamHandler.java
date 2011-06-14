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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class StreamHandler extends Handler {

    private static final String LEVEL_PROPERTY = "manyminds.debug.StreamHandler.level";
    private static final String DEFAULT_LEVEL = Level.INFO.toString();
    private static final String FILTER_PROPERTY = "manyminds.debug.StreamHandler.filter";
    private static final String FORMATTER_PROPERTY = "manyminds.debug.StreamHandler.formatter";
    private static final String DEFAULT_FORMATTER = "manyminds.debug.SimpleFormatter";
    private static final String ENCODING_PROPERTY = "manyminds.debug.StreamHandler.encoding";

    private OutputStreamWriter writer;
    private boolean headHasBeenWritten;
    private boolean tailHasBeenWritten;

    public StreamHandler() {
        this( null, null );
    }

    public StreamHandler( OutputStream anOut, Formatter optionalFormatter ) {
        this( anOut,
              optionalFormatter,
              LEVEL_PROPERTY,
              DEFAULT_LEVEL,
              FILTER_PROPERTY,
              FORMATTER_PROPERTY,
              DEFAULT_FORMATTER,
              ENCODING_PROPERTY );
    }

    protected StreamHandler( OutputStream anOut, Formatter optionalFormatter,
        String levelPropertyName,
        String levelPropertyDefault,
        String filterPropertyName,
        String formatterPropertyName,
        String formatterPropertyDefault,
        String encodingPropertyName ) {

        LogManager theManager = LogManager.getLogManager();
        // Set theLevel
        String level = theManager.getProperty( levelPropertyName );
        if ( level == null ) {
            level = levelPropertyDefault;
        }
        try {
            setLevel( Level.parse( level ) );
        }
        catch ( IllegalArgumentException iae ) {
            setLevel( Level.ALL );
        }

        // Set the Filter if the property is defined.
        String filterClassName = theManager.getProperty( filterPropertyName );
        if ( filterClassName != null ) {
            try {
                Class filterClass = Class.forName( filterClassName );
                if ( ! Filter.class.isAssignableFrom( filterClass ) ) {
                    reportError( "Class specified for filter ('" + filterClassName + "') does not implement Filter!", null, ErrorManager.GENERIC_FAILURE );
                }
                else {
                        setFilter( (Filter) filterClass.newInstance() );
                }
            }
            catch ( ClassNotFoundException cnfe ) {
                reportError( "Class specified for filter ('" + filterClassName + "') not found.", cnfe, ErrorManager.GENERIC_FAILURE );
            }
            catch( IllegalAccessException iae ) {
                reportError( "Unable to access class specified for filter ('" + filterClassName + "').", iae, ErrorManager.GENERIC_FAILURE );
            }
            catch( InstantiationException ie ) {
                reportError( "Problems instantiating class specified for filter ('" + filterClassName + "').", ie, ErrorManager.GENERIC_FAILURE );
            }
        }

        if ( optionalFormatter != null ) {
            setFormatter( optionalFormatter );
        }
        else {
            String formatterName = theManager.getProperty( formatterPropertyName );
            if ( formatterName == null ) {
                formatterName = formatterPropertyDefault;
            }
            try {
                Class formatterClass = Class.forName( formatterName );
                if ( ! Formatter.class.isAssignableFrom( formatterClass ) ) {
                    reportError( "Class specified for formatter ('" + formatterName + "') does not implement Formatter!", null, ErrorManager.GENERIC_FAILURE );
                    setFormatter( new SimpleFormatter() );
                }
                else {
                    setFormatter( (Formatter) formatterClass.newInstance() );
                }
            }
            catch ( ClassNotFoundException cnfe ) {
                reportError( "Class specified for formatter ('" + formatterName + "') not found.", cnfe, ErrorManager.GENERIC_FAILURE );
            }
            catch( IllegalAccessException iae ) {
                reportError( "Unable to access class specified for formatter ('" + formatterName + "').", iae, ErrorManager.GENERIC_FAILURE );
           }
            catch( InstantiationException ie ) {
                reportError( "Problems instantiating class specified for formatter ('" + formatterName + "').", ie, ErrorManager.GENERIC_FAILURE );
            }
        }

        String encoding = theManager.getProperty( encodingPropertyName );
        try {
            setEncoding( encoding, false );
        }
        catch ( UnsupportedEncodingException uee ) {
            // Ignore
        }

        if ( anOut != null ) {
            setOutputStream( anOut );
        }
    }

    public synchronized void close() throws SecurityException {
        LoggingPermission.checkAccess();
        if ( writer != null ) {
            try {
                assureHeadWritten();
                assureTailWritten();
                writer.close();
            }
            catch ( IOException ioe ) {
                reportError( null, ioe, ErrorManager.CLOSE_FAILURE );
            }
            writer = null;
        }
    }

    public synchronized void flush() {
        if ( writer != null ) {
            try {
                writer.flush();
            }
            catch ( IOException ioe ) {
                reportError( null, ioe, ErrorManager.FLUSH_FAILURE );
            }
        }
    }

    public boolean isLoggable( LogRecord record ) {
        boolean retval = ( writer != null && super.isLoggable( record ) );
        return retval;
    }

    public void publish( LogRecord record ) {
        if ( ! isLoggable( record ) ) {
            return;
        }
        try {
            assureHeadWritten();
            writer.write( getFormatter().format( record ) );
        }
        catch ( IOException ioe ) {
            reportError( "Unable to publish record", ioe, ErrorManager.WRITE_FAILURE );
        }
    }

    public void setEncoding( String encoding ) throws UnsupportedEncodingException {
        setEncoding( encoding, true );
    }

    public void setOutputStream( OutputStream anOut ) {
        LoggingPermission.checkAccess();
        if ( writer != null ) {
            /*NONCONFORMANT? - we print out both head and tail; document only
            mentions the tail*/
            close();
        }
        if ( getEncoding() != null ) {
            try {
                writer = new OutputStreamWriter( anOut, getEncoding() );
            }
            catch ( UnsupportedEncodingException uee ) {
                writer = new OutputStreamWriter( anOut );
                clearEncoding();
            }
        }
        else {
            writer = new OutputStreamWriter( anOut );
            setKnownEncoding( writer.getEncoding() );
        }
        headHasBeenWritten = false;
        tailHasBeenWritten = false;
    }

    protected void assureHeadWritten() throws IOException {
        if ( ! headHasBeenWritten ) {
            if ( writer != null ) {
               writer.write( getFormatter().getHead( this ) );
            }
            headHasBeenWritten = true;
        }
    }


    protected void assureTailWritten() throws IOException {
        if ( ! tailHasBeenWritten ) {
            if ( writer != null ) {
               writer.write( getFormatter().getTail( this ) );
            }
            tailHasBeenWritten = true;
        }
    }

}