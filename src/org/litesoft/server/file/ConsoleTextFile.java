package org.litesoft.server.file;

import org.litesoft.commonfoundation.console.Console;
import org.litesoft.commonfoundation.exceptions.*;

import java.io.*;

public class ConsoleTextFile implements Console {

    private final String mFileName;
    private Writer mWriter;

    public ConsoleTextFile( String pFileName ) {
        File zFile = new File( mFileName = pFileName );
        if ( zFile.exists() && (!zFile.isFile() || !zFile.canWrite()) ) {
            throw new IllegalArgumentException( pFileName + " does not appear to be a writable file" );
        }
        try {
            mWriter = FileUtils.createWriter( zFile, false );
        }
        catch ( IOException e ) {
            throw convert( "Opening", e );
        }
    }

    private FileSystemException convert( String pWhat, IOException e ) {
        return new FileSystemException( pWhat + " issue w/ File: " + mFileName, e );
    }

    @Override
    public void print( String pText ) {
        write( getWriter(), pText );
    }

    @Override
    public void println( String pLine ) {
        Writer zWriter = getWriter();
        write( zWriter, pLine );
        write( zWriter, "\n" );
    }

    @Override
    public void close() {
        Closeable zCloseable = mWriter;
        mWriter = null;
        try {
            zCloseable.close();
        }
        catch ( IOException e ) {
            throw convert( "Closing", e );
        }
    }

    private Writer getWriter() {
        if ( mWriter == null ) {
            throw new IllegalStateException( "File '" + mFileName + "' already closed" );
        }
        return mWriter;
    }

    private void write( Writer pWriter, String pText ) {
        try {
            pWriter.write( pText );
        }
        catch ( IOException e ) {
            throw convert( "Writing", e );
        }
    }
}
