package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.server.util.*;

import java.io.*;

public class RelativeFileFromContents extends RelativeFile {
    private String mContents;

    public RelativeFileFromContents( String pRelativeFilePath, String pContents ) {
        super( pRelativeFilePath );
        mContents = Confirm.isNotNull( "Contents", pContents );
    }

    @Override
    public InputStream open()
            throws FileSystemException {
        try {
            return new ByteArrayInputStream( mContents.getBytes( IOUtils.UTF_8 ) );
        }
        catch ( UnsupportedEncodingException e ) {
            throw new FileSystemException( e );
        }
    }
}
