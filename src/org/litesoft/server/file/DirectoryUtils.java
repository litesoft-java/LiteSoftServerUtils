// This Source Code is in the Public Domain per: http://unlicense.org
package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.FileSystemException;

import java.io.*;
import java.nio.file.*;

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

    public static boolean isDirectory( File pDirectory ) {
        if ( pDirectory != null ) {
            if ( pDirectory.isDirectory() ) {
                return true;
            }
            if ( isSymLinkToDirectory( pDirectory ) ) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSymLinkToDirectory( File pDirectory ) {
        Path zPath = pDirectory.toPath();
        boolean zSymbolicLink = Files.isSymbolicLink( zPath );
        if ( zSymbolicLink ) {
            File zDirectory = symbolicLinkToFile( zPath );
            return isDirectory( zDirectory );
        }
        return false;
    }

    private static File symbolicLinkToFile( Path pPath ) {
        try {
            return Files.readSymbolicLink( pPath ).toFile();
        }
        catch ( IOException e ) {
            return null;
        }
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

    public static File findAncestralFile( File pFromDir, String pFilename, String... pFilePrefixes ) {
        Confirm.isNotNull( "FromDir", pFromDir );
        pFilename = Confirm.significant( "Filename", pFilename );
        if ( Currently.isNullOrEmpty( pFilePrefixes ) ) {
            return findFileAncestrally( pFromDir, pFilename );
        }
        for ( String zPrefix : pFilePrefixes ) {
            File zFile = findFileAncestrally( pFromDir, zPrefix + pFilename );
            if ( zFile != null ) {
                return zFile;
            }
        }
        return null;
    }

    private static File findFileAncestrally( File pFromDir, String pFilename ) {
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
