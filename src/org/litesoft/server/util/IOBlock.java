package org.litesoft.server.util;

import java.io.*;

public class IOBlock {
    public static final int BLOCK_SIZE = 1024 * 16; // 16K

    private final int mByteCount;
    private final byte[] mBytes;
    private int mCurrentOffset;

    public IOBlock( int pByteCount, byte[] pBytes ) {
        mByteCount = pByteCount;
        mBytes = pBytes;
    }

    public IOBlock reset() {
        mCurrentOffset = 0;
        return this;
    }

    public int read() {
        return (mCurrentOffset < mByteCount) ? (mBytes[mCurrentOffset++] & 0xff) : -1;
    }

    public void to( OutputStream pOutputStream )
            throws IOException {
        if ( mCurrentOffset < mByteCount ) {
            pOutputStream.write( mBytes, mCurrentOffset, mByteCount - mCurrentOffset );
            mCurrentOffset = mByteCount;
        }
    }

    public static IOBlock from( InputStream pInputStream )
            throws IOException {
        byte[] zBytes = new byte[BLOCK_SIZE];
        int zRead;
        do {
            zRead = pInputStream.read( zBytes );
        } while ( zRead == 0 );
        return (zRead == -1) ? null : new IOBlock( zRead, zBytes );
    }
}
