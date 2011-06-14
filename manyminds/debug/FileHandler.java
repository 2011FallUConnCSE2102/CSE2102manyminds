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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.StringTokenizer;

/**
 * HERE - we should keep the files open and truncate them when reusing them?
 * This would be more efficient in one way, but tricky to get right and use more
 * resources (like file descriptors).
 */
public class FileHandler extends StreamHandler {

    private static final String LEVEL_PROPERTY = "manyminds.debug.FileHandler.level";
    private static final String DEFAULT_LEVEL = Level.ALL.toString();
    private static final String FILTER_PROPERTY = "manyminds.debug.FileHandler.filter";
    private static final String FORMATTER_PROPERTY = "manyminds.debug.FileHandler.formatter";
    private static final String DEFAULT_FORMATTER = "manyminds.debug.XMLFormatter";
    private static final String ENCODING_PROPERTY = "manyminds.debug.FileHandler.encoding";

    private static final String LIMIT_PROPERTY = "manyminds.debug.FileHandler.limit";
    private static final int DEFAULT_LIMIT = -1;
    private static final String FILE_COUNT_PROPERTY = "manyminds.debug.FileHandler.count";
    private static final int DEFAULT_FILE_COUNT = 1;
    private static final String PATTERN_PROPERTY = "manyminds.debug.FileHandler.pattern";
    private static final String DEFAULT_PATTERN = "%t/java.log";
    private static final String APPEND_PROPERTY = "manyminds.debug.FileHandler.append";

    // We use a MessageFormat to generate our file name paths. For these
    // purposes, the arguments are:
    //
    // 0 - system temporary directory
    // 1 - value of the "user.home" property
    // 2 - the generation number
    // 3 - the uniqueness number
    private static final String TEMPDIR_STRING = "{0}";
    private static final String USERHOME_STRING = "{1}";
    private static final String GENERATION_STRING = "{3}";
    private static final String UNIQUENESS_STRING = "{2}";
    private Object[] formatArguments;

    private BufferedOutputStream buffered;
    private int theMaximumFileSize;
    private int theNumberOfGenerations;
    private int theCurrentGenerationNumber;
    private boolean append;

    private boolean hasUniqueness;
    private String theMessageFormatString;
    private String pattern;
    private File theCurrentFile;

    public FileHandler() throws IOException {
        this ( LogManager.getLogManager().getProperty( PATTERN_PROPERTY ),
               null,
               null,
               LogManager.getLogManager().getProperty( APPEND_PROPERTY ) );
    }

    public FileHandler( String aPattern ) throws IOException {
        this( aPattern,
              null,
              null,
              LogManager.getLogManager().getProperty( APPEND_PROPERTY ) );
    }

    public FileHandler( String aPattern, boolean append ) throws IOException {
        this( aPattern,
              null,
              null,
              append );
    }

    public FileHandler( String aPattern, int limit, int count ) throws IOException {
        this ( aPattern,
               new Integer( limit ),
               new Integer( count ),
               LogManager.getLogManager().getProperty( APPEND_PROPERTY ) );
    }

    public FileHandler( String aPattern, int limit, int count, boolean append ) throws IOException {
        this ( aPattern, new Integer( limit ), new Integer( count ), append );
    }

    public void close() {
        super.close();
        // HERE - what do we do different than our super-class?
    }

    public synchronized void publish( LogRecord record ) {
        super.publish( record );
        try {
            flush();
            if ( theMaximumFileSize > 0 && theCurrentFile.length() > theMaximumFileSize ) {
                switchFiles();
            }
        }
        catch ( IOException ioe ) {
            reportError( "Problems publishing record.", ioe, ErrorManager.WRITE_FAILURE );
        }

    }

    private FileHandler( String aPattern, Integer aLimit, Integer aFileCount, String appendString ) throws IOException {
        this( aPattern, aLimit, aFileCount, appendString == null ? false : appendString.equalsIgnoreCase( "true " ) );
    }

    private FileHandler( String aPattern, Integer aLimit, Integer aFileCount, boolean doAppend ) throws IOException {
        super( null,
               null,
               LEVEL_PROPERTY,
               DEFAULT_LEVEL,
               FILTER_PROPERTY,
               FORMATTER_PROPERTY,
               DEFAULT_FORMATTER,
               ENCODING_PROPERTY );

        LogManager manager = LogManager.getLogManager();

        append = doAppend;

        pattern = DEFAULT_PATTERN;
        if ( aPattern != null ) {
            pattern = aPattern;
        }

        int limit = DEFAULT_LIMIT;
        if ( aLimit != null ) {
            limit = aLimit.intValue();
        }
        else {
            String limitString = manager.getProperty( LIMIT_PROPERTY );
            if ( limitString != null ) {
                try {
                    limit = Integer.parseInt( limitString );
                }
                catch ( NumberFormatException nfe ) {
                    // Use the default
                }
            }
        }
        if ( limit == 0 ) {
            throw new IllegalArgumentException( "File size must be at least 1 or not constrained" );
        }

        int numberOfFiles = DEFAULT_FILE_COUNT;
        if ( aFileCount != null ) {
            numberOfFiles = aFileCount.intValue();
        }
        else {
            String fileCountString = manager.getProperty( FILE_COUNT_PROPERTY );
            if ( fileCountString != null ) {
                try {
                    numberOfFiles = Integer.parseInt( fileCountString );
                }
                catch ( NumberFormatException nfe ) {
                    // Use the default
                }
            }
        }
        if ( numberOfFiles <= 0 ) {
            throw new IllegalArgumentException( "Must have at least one file to write to!" );
        }

        theMaximumFileSize = limit;
        theNumberOfGenerations = numberOfFiles;

        formatArguments = new Object[] {
            getSystemTemporaryDirectoryPath(),
            System.getProperty( "user.home" ),
            "0",
            "0" };

        theMessageFormatString = createMessageFormatString();
        // Generate the new file name
        File createdFile = null;
        int uniqueness = 0;
        do {
            String fileName = MessageFormat.format( theMessageFormatString, formatArguments );
            File temp = new File( fileName );
            if ( temp.createNewFile() ) {
                FileOutputStream fos = new FileOutputStream( fileName, append );
                //BufferedOutputStream o = new BufferedOutputStream( new GZIPOutputStream(fos) );
                BufferedOutputStream o = new BufferedOutputStream( fos );
                createdFile = temp;
                buffered = o;
            }
            else if ( ! hasUniqueness ) {
                pattern += ".%u";
                theMessageFormatString = createMessageFormatString();
                hasUniqueness = true;
            }
            else {
                formatArguments[ 2 ] = "" + ++uniqueness;
            }
        } while ( createdFile == null );
        setOutputStream( buffered );
        theCurrentFile = createdFile;
    }

    private String createMessageFormatString() {
        // Parse the pattern string. We use a MessageFormat to generate our
        // file names. For these purposes, the arguments are:
        StringTokenizer st = new StringTokenizer( pattern, "%/{}", true );
        StringBuffer buffer = new StringBuffer();
        boolean lastWasPercent = false;
        boolean hasGeneration = false;
        while ( st.hasMoreTokens() ) {
            String currentToken = st.nextToken();
            if ( currentToken.equals( "/" ) ) {
                if ( lastWasPercent ) {
                    // They did '%/', which makes no sense!
                    throw new IllegalArgumentException( "Character '/' not a valid special component!" );
                }
                buffer.append( File.separator );
                lastWasPercent = false;
            }
            else if ( currentToken.equals( "{" ) ) {
                buffer.append( "\\{" );
                lastWasPercent = false;
            }
            else if ( currentToken.equals( "}" ) ) {
                buffer.append( "\\}" );
                lastWasPercent = false;
            }
            else if ( currentToken.equals( "%" ) ) {
                if ( lastWasPercent ) {
                    buffer.append( '%' );
                    lastWasPercent = false;
                }
                else {
                    lastWasPercent = true;
                }
            }
            else {
                if ( lastWasPercent ) {
                    char nextChar = currentToken.charAt( 0 );
                    if ( nextChar == 't' ) {
                        buffer.append( TEMPDIR_STRING );
                    }
                    else if ( nextChar == 'h' ) {
                        buffer.append( USERHOME_STRING );
                    }
                    else if ( nextChar == 'H' ) {
                        buffer.append( System.getProperty( "manyminds.home" ) );
                        buffer.append( System.getProperty( "file.separator" ) );
                        buffer.append( "LogFiles" );
                    }
                    else if ( nextChar == 'g' ) {
                        buffer.append( GENERATION_STRING );
                        hasGeneration = true;
                    }
                    else if ( nextChar == 'u' ) {
                        buffer.append( UNIQUENESS_STRING );
                        hasUniqueness = true;
                    }
                    else if (nextChar == 'U' ) {
                        buffer.append( System.getProperty("user.name") );
                    }
                    else {
                        throw new IllegalArgumentException( "Character '" + nextChar + "' not a valid special component!" );
                    }
                    if ( currentToken.length() > 1 ) {
                        buffer.append( currentToken.substring( 1 ) );
                    }
                }
                else {
                    buffer.append( currentToken );
                }
                lastWasPercent = false;
            }
        }
        if ( ! hasGeneration && theNumberOfGenerations > 1 ) {
            buffer.append( '.' );
            buffer.append( GENERATION_STRING );
        }
        if ( ! hasUniqueness ) {
            buffer.append( '.' );
            buffer.append( UNIQUENESS_STRING );
        }
        return buffer.toString();
    }

    private void switchFiles() throws IOException {
        buffered.close();

        theCurrentGenerationNumber += 1;
        if ( theCurrentGenerationNumber > theNumberOfGenerations ) {
           theCurrentGenerationNumber %= theNumberOfGenerations;
        }
        formatArguments[ 3 ] = "" + theCurrentGenerationNumber;

        // Generate the new file name and use it
        String fileName = MessageFormat.format( theMessageFormatString, formatArguments );
        FileOutputStream fos = new FileOutputStream( fileName, append );
//        buffered = new BufferedOutputStream( new GZIPOutputStream(fos) );
        buffered = new BufferedOutputStream( fos );
        setOutputStream( buffered );
        theCurrentFile = new File( fileName );
    }

    private String getSystemTemporaryDirectoryPath() {

        return System.getProperty( "java.io.tmpdir" );
    }
}