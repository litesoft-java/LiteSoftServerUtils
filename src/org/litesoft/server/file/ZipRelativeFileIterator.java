package org.litesoft.server.file;

import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.server.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipRelativeFileIterator extends RelativeFileIterator {
    private ZipFile mZipFile;
    private Enumeration<? extends ZipEntry> mEnumeration;

    public ZipRelativeFileIterator( File pZipFile )
            throws IOException {
        mEnumeration = (mZipFile = new ZipFile( pZipFile )).entries();
    }

    @Override
    public boolean hasNext() {
        return (mEnumeration != null) && mEnumeration.hasMoreElements();
    }

    @Override
    public RelativeFile next() {
        if ( hasNext() ) {
            return new ZipRelativeFile( mEnumeration.nextElement() );
        }
        return super.next();
    }

    @Override
    public void dispose() {
        Closeables.dispose( mZipFile );
        mZipFile = null;
        mEnumeration = null;
    }

    private class ZipRelativeFile extends RelativeFile {
        private ZipEntry mZipEntry;

        public ZipRelativeFile( ZipEntry pZipEntry ) {
            super( pZipEntry.getName() );
            mZipEntry = pZipEntry;
        }

        @Override
        public InputStream open()
                throws FileSystemException {
            try {
                return mZipFile.getInputStream( mZipEntry );
            }
            catch ( IOException e ) {
                throw new FileSystemException( e );
            }
        }
    }
}

