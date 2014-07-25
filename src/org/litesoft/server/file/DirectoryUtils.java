// This Source Code is in the Public Domain per: http://unlicense.org
package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;

import java.io.*;

@SuppressWarnings({"UnusedDeclaration"})
public class DirectoryUtils {
    public static File ensureExistsAndMutable( String pWhat, File pDirectory ) {
        if ( !pDirectory.exists() ) {
            makeDirs( pDirectory );
        }
        return assertMutable( pWhat, pDirectory );
    }

    public static File assertExists( String pWhat, File pDirectory ) {
        if ( pDirectory.exists() ) {
            return pDirectory;
        }
        throw IllegalArgument.exception( pWhat, "'" + pDirectory.getAbsolutePath() + "' Does NOT Exist" );
    }

    public static boolean existsThenAssertMutable( String pWhat, File pDirectory ) {
        if ( !pDirectory.exists() ) {
            return false;
        }
        assertMutable( pWhat, pDirectory );
        return true;
    }

    public static void makeDirs( File pDirectory ) {
        if ( !pDirectory.mkdirs() && !pDirectory.isDirectory() ) {
            throw new FileSystemException( "Unable to create: " + pDirectory.getAbsolutePath() );
        }
    }

    public static File assertMutable( String pWhat, File pDirectory ) {
        if ( acceptableMutable( pDirectory ) ) {
            return pDirectory;
        }
        throw IllegalArgument.exception( pWhat, "'" + pDirectory.getAbsolutePath() + "' Not Mutable" );
    }

    public static boolean acceptableMutable( File pDirectory ) {
        return pDirectory.isDirectory() && pDirectory.canRead() && pDirectory.canWrite();
    }

    public static void purge( File pDirectory )
            throws FileSystemException {
        Confirm.isNotNull( "Directory", pDirectory );
        if ( pDirectory.isDirectory() ) {
            LowLevelDeleteAllEntries( pDirectory );
            if ( !pDirectory.delete() || pDirectory.exists() ) {
                throw new FileSystemException( "Unable to delete: " + pDirectory.getAbsolutePath() );
            }
        }
    }

    public static void deleteAllEntries( File pDirectory )
            throws FileSystemException {
        Confirm.isNotNull( "Directory", pDirectory );
        if ( pDirectory.isDirectory() ) {
            LowLevelDeleteAllEntries( pDirectory );
        }
    }

    private static void LowLevelDeleteAllEntries( File pDirectory )
            throws FileSystemException {
        String[] files = pDirectory.list();
        for ( String file : files ) {
            File entry = new File( pDirectory, file );
            if ( entry.isDirectory() ) {
                purge( entry );
            } else {
                FileUtils.deleteIfExists( entry );
            }
        }
    }

    public static File findAncestralFile( File pFromDir, String pFilename ) {
        pFilename = Confirm.significant( "Filename", pFilename );
        File zFile;
        while ( !(zFile = new File( pFromDir, pFilename )).isFile() ) {
            if ( null == (pFromDir = pFromDir.getParentFile()) ) {
                return null;
            }
        }
        return zFile;
    }
}
