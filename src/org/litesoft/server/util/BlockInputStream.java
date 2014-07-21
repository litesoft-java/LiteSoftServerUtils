package org.litesoft.server.util;

import java.io.*;
import java.util.*;

public class BlockInputStream extends InputStream {
    private final Iterator<IOBlock> mBlocks;
    private IOBlock mCurrent;

    public BlockInputStream( List<IOBlock> pBlocks ) {
        mBlocks = pBlocks.iterator();
        nextBlock();
    }

    private boolean nextBlock() {
        if ( mBlocks.hasNext() ) {
            mCurrent = mBlocks.next().reset();
            return true;
        }
        mCurrent = null;
        return false;
    }

    @Override
    public int read()
            throws IOException {
        if ( mCurrent == null ) {
            return -1;
        }
        int zRead = mCurrent.read();
        if ( (zRead == -1) && nextBlock() ) { // Left to Right Critical!
            zRead = mCurrent.read();
        }
        return zRead;
    }
}
