package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;

import java8.util.function.*;

import java.io.*;
import java.util.*;

public class CanonicalDirectories implements Supplier<List<File>> {
    private final Map<String, Triad> mTriadsByUserPaths = Maps.newLinkedHashMap();

    public List<String> getUserPaths() {
        return Lists.from( mTriadsByUserPaths.keySet() ); // Copies it
    }

    @Override
    public List<File> get() {
        List<File> zFiles = Lists.newArrayList();
        for ( Triad zTriad : mTriadsByUserPaths.values() ) {
            zFiles.add( zTriad.getFile() );
        }
        return zFiles;
    }

    public File add( String pUserPath ) {
        File zFile = FileUtils.canonicalFileOrNull( Confirm.isNotNull( "UserPath", pUserPath ) );
        if ( (zFile != null) && zFile.isDirectory() ) {
            mTriadsByUserPaths.put( pUserPath, checkForRelated( new Triad( zFile, pUserPath ) ) );
            return zFile;
        }
        return null;
    }

    private Triad checkForRelated( Triad pTriad ) {
        for ( Triad zTriad : mTriadsByUserPaths.values() ) {
            zTriad.validateNotRelated( pTriad );
        }
        return pTriad;
    }

    private static class Triad {
        private static final String SEPARATOR = File.separator;
        private final String mUserPath;
        private final File mCanonicalDir;
        private final String mCanonicalPath;

        private Triad( File pCanonicalDir, String pUserPath ) {
            mUserPath = pUserPath;
            mCanonicalDir = pCanonicalDir;
            mCanonicalPath = canonicalize( pCanonicalDir.getAbsolutePath() );
        }

        private String canonicalize( String pPath ) {
            return pPath.endsWith( SEPARATOR ) ? pPath : (pPath + SEPARATOR);
        }

        public File getFile() {
            return mCanonicalDir;
        }

        public void validateNotRelated( Triad them ) {
            validateNotSamePath( them );
            this.validateNotParentOf( them );
            them.validateNotParentOf( this );
        }

        private void validateNotSamePath( Triad them ) {
            if ( this.mUserPath.equals( them.mUserPath ) ) {
                throw new IllegalArgumentException( "Duplicate User Path '" + mUserPath + "'" );
            }
            if ( this.mCanonicalPath.equals( them.mCanonicalPath ) ) {
                throw new IllegalArgumentException( "User Path '" + this.mUserPath + "' and User Path '" + this.mUserPath + "' both with '" + mCanonicalPath + "'" );
            }
        }

        private void validateNotParentOf( Triad them ) {
            if ( them.mCanonicalPath.startsWith( this.mCanonicalPath ) ) {
                throw new IllegalStateException( "User Path '" + this.mUserPath + "' appears to be a parent of '" + them.mUserPath + "'!" +
                                                 "\n     Parent: " + this.mCanonicalPath +
                                                 "\n      Child: " + them.mCanonicalPath );
            }
        }

        @Override
        public String toString() {
            return "UserPath: '" + mUserPath + "' -> " + mCanonicalPath;
        }
    }
}
