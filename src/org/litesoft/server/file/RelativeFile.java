package org.litesoft.server.file;

import org.litesoft.commonfoundation.exceptions.*;

import java.io.*;

public abstract class RelativeFile {
    private final String mRelativeFilePath;

    public RelativeFile( String pRelativeFilePath ) {
        mRelativeFilePath = pRelativeFilePath;
    }

    public String getRelativeFilePath() {
        return mRelativeFilePath;
    }

    abstract public InputStream open()
            throws FileSystemException;
}
