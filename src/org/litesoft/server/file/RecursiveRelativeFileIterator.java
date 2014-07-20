package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.io.*;
import java.util.*;

public class RecursiveRelativeFileIterator extends RelativeFileIterator {
    private final File mBaseDir;
    private final Iterator<String> mRelativeFilePaths;

    public RecursiveRelativeFileIterator( File pBaseDir ) {
        mBaseDir = DirectoryUtils.assertExists( "BaseDir", Confirm.isNotNull( "BaseDir", pBaseDir ) );
        mRelativeFilePaths = new RecursiveCollector( mBaseDir ).getRelativeFilePaths().iterator();
    }

    private static class RecursiveCollector {
        private final List<String> mRelativeFilePaths = Lists.newArrayList();

        public RecursiveCollector( File pBaseDir ) {
            addDirectory( pBaseDir, "" );
        }

        private void addDirectory( File pFromDir, String pRelativePrefix ) {
            addFiles( pFromDir, pRelativePrefix );
            addDirectories( pFromDir, pRelativePrefix );
        }

        private void addFiles( File pFromDir, String pRelativePrefix ) {
            for ( String zEntry : ConstrainTo.notNull( pFromDir.list( FileUtils.FILES_ONLY ) ) ) {
                mRelativeFilePaths.add( Paths.append( pRelativePrefix, zEntry ) );
            }
        }

        private void addDirectories( File pFromDir, String pRelativePrefix ) {
            for ( String zEntry : ConstrainTo.notNull( pFromDir.list( FileUtils.DIRECTORIES_ONLY ) ) ) {
                addDirectory( new File( pFromDir, zEntry ), Paths.append( pRelativePrefix, zEntry ) );
            }
        }

        public List<String> getRelativeFilePaths() {
            return mRelativeFilePaths;
        }
    }

    @Override
    public boolean hasNext() {
        return mRelativeFilePaths.hasNext();
    }

    @Override
    public RelativeFile next() {
        return new FileRelativeFile( mBaseDir, mRelativeFilePaths.next() );
    }

    private static class FileRelativeFile extends RelativeFile {
        private final File mBaseDir;

        private FileRelativeFile( File pBaseDir, String pRelativeFilePath ) {
            super( pRelativeFilePath );
            mBaseDir = pBaseDir;
        }

        @Override
        public InputStream open()
                throws FileSystemException {
            try {
                return new FileInputStream( new File( mBaseDir, getRelativeFilePath() ) );
            }
            catch ( FileNotFoundException e ) {
                throw new FileSystemException( e );
            }
        }
    }
}
