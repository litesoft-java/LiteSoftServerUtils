package org.litesoft.server.util;

import org.litesoft.commonfoundation.exceptions.*;

import java.io.*;
import java.util.*;

public class IOUtils {
    public static final String UTF_8 = "UTF-8";

    public static BufferedReader createReader( InputStream pInputStream )
            throws IOException {
        return new BufferedReader( new InputStreamReader( pInputStream, UTF_8 ) );
    }

    public static BufferedWriter createWriter( OutputStream pOS )
            throws IOException {
        return new BufferedWriter( new OutputStreamWriter( pOS, UTF_8 ) );
    }

    public static String[] loadTextFile( BufferedReader pReader )
            throws FileSystemException {
        try {
            List<String> lines = new LinkedList<String>();
            boolean closed = false;
            try {
                for ( String line; null != (line = pReader.readLine()); ) {
                    lines.add( line );
                }
                closed = true;
                pReader.close();
            }
            finally {
                if ( !closed ) {
                    Closeables.dispose( pReader );
                }
            }
            return lines.toArray( new String[lines.size()] );
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    public static void storeBytes( List<byte[]> pContents, OutputStream pOutputStream ) {
        try {
            boolean closed = false;
            try {
                for ( byte[] zBlock : pContents ) {
                    pOutputStream.write( zBlock );
                }
                closed = true;
                pOutputStream.close();
            }
            finally {
                if ( !closed ) {
                    Closeables.dispose( pOutputStream );
                }
            }
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    public static List<byte[]> loadBytes( InputStream pInputStream ) {
        try {
            List<byte[]> blocks = new LinkedList<byte[]>();
            boolean closed = false;
            try {
                for ( byte[] zBlock; null != (zBlock = readBlock( pInputStream )); ) {
                    blocks.add( zBlock );
                }
                closed = true;
                pInputStream.close();
            }
            finally {
                if ( !closed ) {
                    Closeables.dispose( pInputStream );
                }
            }
            return blocks;
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    private static byte[] readBlock( InputStream pInputStream )
            throws IOException {
        byte[] zBlock = new byte[4096];
        int zCount;
        do {
            if ( zBlock.length == (zCount = pInputStream.read( zBlock )) ) {
                return zBlock;
            }
        } while ( zCount == 0 );
        if ( zCount == -1 ) {
            return null;
        }
        byte[] zShortBlock = new byte[zCount];
        System.arraycopy( zBlock, 0, zShortBlock, 0, zCount );
        return zShortBlock;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static void drain( InputStream pInputStream ) {
        try {
            while ( -1 != pInputStream.read() ) {
                ;
            }
        }
        catch ( IOException whatever ) {
            // Whatever
        }
    }
}
