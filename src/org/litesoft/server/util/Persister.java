package org.litesoft.server.util;

import org.litesoft.commonfoundation.exceptions.*;

import java.io.*;

/**
 * Abstraction for a FileSystem.
 * <p/>
 * Note: For most read and write operations (and copy) it should be assumed that the whole file will be loaded in to memory!
 */
public abstract class Persister {
    abstract public String[] getTextFile( String pPath )
            throws FileSystemException;

    abstract public void putTextFile( String pPath, String[] pLines )
            throws FileSystemException;

    abstract public InputStream getFile( String pPath )
            throws FileSystemException;

    abstract public void putFile( String pPath, InputStream pFileContents )
            throws FileSystemException;

    abstract public String[] getDirectories( String pDirectoryNamePrefix )
            throws FileSystemException;

    abstract public String[] getFiles( String pFilesSubDirectory, String pFileNamePrefix, String pFileExtension )
            throws FileSystemException;

    abstract public String[] getAllFilesUnder( String pFilesSubDirectory )
            throws FileSystemException;

    public void copyFile( String pSourcePath, String pDestinationPath )
            throws FileSystemException {
        putFile( pDestinationPath, getFile( pSourcePath ) );
    }

    public void copyFile( Persister pSourcePersister, String pSourcePath, String pDestinationPath )
            throws FileSystemException {
        putFile( pDestinationPath, pSourcePersister.getFile( pSourcePath ) );
    }

    public void copyFile( String pSourcePath, Persister pDestinationPersister, String pDestinationPath )
            throws FileSystemException {
        pDestinationPersister.copyFile( this, pSourcePath, pDestinationPath );
    }

    abstract public void deleteDirectory( String pPath )
            throws FileSystemException;

    abstract public void deleteFile( String pPath )
            throws FileSystemException;

    abstract public boolean isReadable( String pPath )
            throws FileSystemException;
}
