package org.litesoft.server.file;

import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.server.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipRelativeFileIterator extends RelativeFileIterator {
    private ZipFile mZipFile;
    private Enumeration<? extends ZipEntry> mEnumeration;
    private ZipEntry mZipEntry;

    public ZipRelativeFileIterator( File pZipFile )
            throws IOException {
        mEnumeration = (mZipFile = new ZipFile( pZipFile )).entries();
        mZipEntry = nextFile();
    }

    private ZipEntry nextFile() {
        if ( mEnumeration != null ) {
            while ( mEnumeration.hasMoreElements() ) {
                ZipEntry zZipEntry = mEnumeration.nextElement();
                if ( !zZipEntry.isDirectory() ) {
                    return zZipEntry;
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return (mZipEntry != null);
    }

    @Override
    public RelativeFile next() {
        if ( !hasNext() ) {
            return super.next();
        }
        RelativeFile zRelativeFile = new ZipRelativeFile( mZipEntry );
        mZipEntry = nextFile();
        return zRelativeFile;
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

