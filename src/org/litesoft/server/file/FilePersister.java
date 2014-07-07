package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.server.util.*;

import java.io.*;
import java.util.*;

public class FilePersister extends Persister {
    private final String mRootPath;

    public FilePersister( String pRootPath ) {
        mRootPath = pRootPath;
    }

    @Override
    public String[] getTextFile( String pPath )
            throws FileSystemException {
        return FileUtils.loadTextFile( new File( mRootPath, pPath ) );
    }

    @Override
    public void putTextFile( String pPath, String[] pLines )
            throws FileSystemException {
        FileUtils.storeTextFile( new File( mRootPath, pPath ), pLines );
    }

    @Override
    public InputStream getFile( String pPath )
            throws FileSystemException {
        try {
            return FileUtils.createInputStream( new File( mRootPath, pPath ) );
        }
        catch ( IOException e ) {
            throw new FileSystemException( "Unable to create InputStream: " + pPath );
        }
    }

    @Override
    public void putFile( String pPath, InputStream pFileContents )
            throws FileSystemException {
        FileUtils.storeFile( new File( mRootPath, pPath ), IOUtils.loadBytes( pFileContents ) );
    }

    @Override
    public String[] getDirectories( final String pDirectoryNamePrefix )
            throws FileSystemException {
        return Strings.deNull( new File( mRootPath ).list( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.startsWith( pDirectoryNamePrefix ) && new File( dir, name ).isDirectory();
            }
        } ) );
    }

    @Override
    public String[] getFiles( String pFilesSubDirectory, final String pFileNamePrefix, final String pFileExtension )
            throws FileSystemException {
        File zDir = Currently.insignificant( pFilesSubDirectory ) ? new File( mRootPath ) : new File( mRootPath, pFilesSubDirectory );
        return Strings.deNull( zDir.list( new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return name.startsWith( pFileNamePrefix ) && name.endsWith( pFileExtension ) && new File( dir, name ).isFile();
            }
        } ) );
    }

    @Override
    public String[] getAllFilesUnder( String pFilesSubDirectory )
            throws FileSystemException {
        List<String> zCollector = Lists.newArrayList();
        populateAllFiles( zCollector, Confirm.significant( "FilesSubDirectory", pFilesSubDirectory ) );
        return zCollector.toArray( new String[zCollector.size()] );
    }

    private void populateAllFiles( List<String> pFileCollector, String pFilesSubDirectory ) {
        File zDir = new File( mRootPath, pFilesSubDirectory );
        String[] zEntries = zDir.list();
        if ( zEntries != null ) {
            for ( String zEntry : zEntries ) {
                String zRelativePath = pFilesSubDirectory + "/" + zEntry;
                File zFile = new File( zDir, zEntry );
                if ( zFile.isFile() ) {
                    pFileCollector.add( zRelativePath );
                } else if ( zFile.isDirectory() ) {
                    populateAllFiles( pFileCollector, zRelativePath );
                } else {
                    throw new FileSystemException( "Unknown file 'type' (neither 'File' nor 'Directory': " + zFile.getAbsolutePath() );
                }
            }
        }
    }

    @Override
    public void deleteDirectory( String pPath )
            throws FileSystemException {
        File zFile = new File( mRootPath, pPath );
        if ( zFile.exists() ) {
            if ( !zFile.isDirectory() ) {
                throw new IllegalArgumentException( "'" + pPath + "' does not appear to be a directory" );
            }
            deleteDirectory( zFile );
        }
    }

    @Override
    public void deleteFile( String pPath )
            throws FileSystemException {
        FileUtils.deleteIfExists( new File( mRootPath, pPath ) );
    }

    private void deleteDirectory( File pDirectory )
            throws FileSystemException {
        File[] zEntries = pDirectory.listFiles();
        if ( zEntries != null ) {
            for ( File zEntry : zEntries ) {
                if ( zEntry.isFile() ) {
                    FileUtils.deleteIfExists( zEntry );
                } else if ( zEntry.isDirectory() ) {
                    deleteDirectory( zEntry );
                } else {
                    throw new FileSystemException( "Unknown file 'type'; neither 'File' nor 'Directory'; to delete: " + zEntry.getAbsolutePath() );
                }
            }
        }
        if ( !pDirectory.delete() || pDirectory.exists() ) {
            throw new FileSystemException( "Unable to delete Directory: " + pDirectory.getAbsolutePath() );
        }
    }

    @Override
    public boolean isReadable( String pPath )
            throws FileSystemException {
        return new File( mRootPath, pPath ).canRead();
    }

    @Override
    public String toString() {
        return "Files(under: " + mRootPath + ")";
    }
}
