package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.commonfoundation.typeutils.Objects;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.server.util.*;

import java.io.*;
import java.util.*;

public class FileUtils {
    public enum Change {New, Updated}

    public static final FilenameFilter DIRECTORIES_ONLY = new FilenameFilter() {
        @Override
        public boolean accept( File dir, String name ) {
            return new File( dir, name ).isDirectory();
        }
    };

    public static final FilenameFilter FILES_ONLY = new FilenameFilter() {
        @Override
        public boolean accept( File dir, String name ) {
            return new File( dir, name ).isFile();
        }
    };

    public static String currentWorkingDirectory() {
        return System.getProperty( "user.dir" );
    }

    public static String requiredSystemPropertyDirectory( String pPropertyName ) {
        return Confirm.significant( "System.getProperty(\"" + pPropertyName + "\")", System.getProperty( pPropertyName ) );
    }

    public static File canonicalFileOrNull( String pPath ) {
        return Currently.isNotNullOrEmpty( pPath ) ? canonicalFileOrNull( new File( pPath ) ) : null;
    }

    public static File canonicalFileOrNull( File pFile ) {
        try {
            if ( pFile != null ) {
                return pFile.getCanonicalFile();
            }
        }
        catch ( IOException e ) {
            // Fall Thru
        }
        return null;
    }

    public static String getExtension( File pFile ) {
        return (pFile == null) ? null : Paths.getExtension( pFile.getAbsolutePath() );
    }

    public static BufferedWriter createWriter( File pFile, boolean pAppend )
            throws IOException {
        Confirm.isNotNull( "File", pFile );
        return IOUtils.createWriter( new FileOutputStream( insureParent( pFile ), pAppend ) );
    }

    public static BufferedReader createReader( File pFile )
            throws IOException {
        Confirm.isNotNull( "File", pFile );
        return IOUtils.createReader( new FileInputStream( pFile ) );
    }

    public static OutputStream createOutputStream( File pFile )
            throws IOException {
        Confirm.isNotNull( "File", pFile );
        return new FileOutputStream( insureParent( pFile ) );
    }

    public static InputStream createInputStream( File pFile )
            throws IOException {
        Confirm.isNotNull( "File", pFile );
        return new FileInputStream( pFile );
    }

    public static OutputStream asOutputStream( File pFile )
            throws FileSystemException {
        try {
            return createOutputStream( pFile );
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    public static File assertFileCreatable( File pExpectedFile )
            throws FileSystemException {
        Confirm.isNotNull( "ExpectedFile", pExpectedFile );
        File zParentFile = pExpectedFile.getParentFile();
        Confirm.isNotNull( "Parent of ExpectedFile: " + pExpectedFile, zParentFile );
        Confirm.isTrue( "Unable to write to: " + zParentFile, zParentFile.canWrite() );
        return pExpectedFile;
    }

    public static File insureParent( File pExpectedFile )
            throws FileSystemException {
        Confirm.isNotNull( "ExpectedFile", pExpectedFile );
        File zParentFile = pExpectedFile.getParentFile();
        if ( zParentFile != null ) {
            insure( zParentFile );
        }
        return pExpectedFile;
    }

    public static File insure( File pExpectedDir )
            throws FileSystemException {
        Confirm.isNotNull( "ExpectedDir", pExpectedDir );
        if ( !pExpectedDir.isDirectory() ) {
            if ( pExpectedDir.exists() ) {
                throw new FileSystemException( "Exists, but is Not a Directory: " + pExpectedDir.getAbsolutePath() );
            }
            if ( !pExpectedDir.mkdirs() || !pExpectedDir.isDirectory() ) {
                throw new FileSystemException( "Unable to create Directory: " + pExpectedDir.getAbsolutePath() );
            }
        }
        return pExpectedDir;
    }

    /**
     * @return Null if already exists and Not changed, otherwise either New of Updated!
     */
    public static Change storeFile( File pFile, List<byte[]> pContents )
            throws FileSystemException {
        if ( pFile.exists() && areEqual( loadFile( pFile ), pContents ) ) {
            return null;
        }
        File zNewFile = asNewFile( pFile );
        try {
            IOUtils.storeBytes( pContents, createOutputStream( zNewFile ) );
        }
        catch ( IOException e ) {
            throw new FileSystemException( "Unable to create OutputStream: " + zNewFile.getAbsolutePath() );
        }
        return rollIn( zNewFile, pFile, asBackupFile( pFile ) ) ? Change.Updated : Change.New;
    }

    public static List<byte[]> loadFile( File pFile )
            throws FileSystemException {
        Confirm.isNotNull( "File", pFile );
        try {
            if ( !pFile.exists() ) {
                throw new FileNotFoundException( pFile.getAbsolutePath() );
            }
            return IOUtils.loadBytes( createInputStream( pFile ) );
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    public static String[] loadTextFile( File pFile )
            throws FileSystemException {
        Confirm.isNotNull( "File", pFile );
        try {
            if ( !pFile.exists() ) {
                throw new FileNotFoundException( pFile.getAbsolutePath() );
            }
            return IOUtils.loadTextFile( createReader( pFile ) );
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    /**
     * @return Null if already exists and Not changed, otherwise either New of Updated!
     */
    public static Change storeTextFile( File pFile, String... pLines )
            throws FileSystemException {
        return storeTextFile( null, pFile, pLines );
    }

    /**
     * @return Null if already exists and Not changed, otherwise either New of Updated!
     */
    public static Change storeTextFile( PrintStream pPrintStream, File pFile, String... pLines )
            throws FileSystemException {
        if ( pFile.exists() && Currently.areEqual( loadTextFile( pFile ), pLines ) ) {
            return null;
        }
        File zNewFile = asNewFile( pFile );
        addLines( zNewFile, false, pLines );
        Change zChange = rollIn( zNewFile, pFile, asBackupFile( pFile ) ) ? Change.Updated : Change.New;
        report( zChange, pPrintStream );
        return zChange;
    }

    public static File asNewFile( File pFile ) {
        return new File( pFile.getAbsolutePath() + ".new" );
    }

    public static File asBackupFile( File pFile ) {
        return new File( pFile.getAbsolutePath() + ".bak" );
    }

    public static void report( Change pChange, PrintStream pPrintStream ) {
        if ( pPrintStream != null ) {
            pPrintStream.print( " --------------------> " + pChange + "!" );
        }
    }

    /**
     * Roll in an Update.
     *
     * @return true if replaced an existing!
     */
    public static boolean rollIn( File pNewFile, File pTargetFile, File pBackupFile )
            throws FileSystemException {
        Confirm.isNotNull( "NewFile", pNewFile );
        Confirm.isNotNull( "TargetFile", pTargetFile );
        Confirm.isNotNull( "BackupFile", pBackupFile );
        if ( !pNewFile.exists() ) {
            throw new FileSystemException( "Does not Exist: " + pNewFile.getPath() );
        }
        boolean targetExisted = false;
        if ( pTargetFile.exists() ) {
            targetExisted = true;
            deleteIfExists( pBackupFile );
            renameFromTo( pTargetFile, pBackupFile );
        }
        try {
            renameFromTo( pNewFile, pTargetFile );
            return targetExisted;
        }
        catch ( FileSystemException e ) {
            if ( targetExisted ) {
                attemptToRollBack( pNewFile, pTargetFile, pBackupFile );
            }
            throw e;
        }
    }

    public static void deleteIfExists( File pFile )
            throws FileSystemException {
        Confirm.isNotNull( "File", pFile );
        if ( pFile.isFile() ) {
            if ( !pFile.delete() || pFile.exists() ) {
                throw new FileSystemException( "Unable to delete File: " + pFile.getAbsolutePath() );
            }
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private static void attemptToRollBack( File pNewFile, File pTargetFile, File pBackupFile ) {
        switch ( (pNewFile.exists() ? 0 : 4) + (pTargetFile.exists() ? 0 : 2) + (pBackupFile.exists() ? 0 : 1) ) {
            // What Happen'd
            case 0: // There: ------------- Nobody --------------
            case 2: // There:            pTargetFile
            case 4: // There: pNewFile
            case 6: // There: pNewFile & pTargetFile
            case 7: // There: pNewFile & pTargetFile & pBackupFile
                return;
            case 3: // There:            pTargetFile & pBackupFile
                pTargetFile.renameTo( pNewFile );
                // Fall Thru
            case 1: // There:                          pBackupFile
            case 5: // There: pNewFile               & pBackupFile
                pBackupFile.renameTo( pTargetFile );
                break;
        }
    }

    /**
     * This method will rename the pSourceFile to the pDestinationFile name.
     * <p/>
     * It is Inherently fragile when dealing with multiple processes playing
     * in the same file system name spaces.  It should therefore NOT be used
     * in a multi-process creation-consumption shared file system name space.
     * <p/>
     * Specifically there are two windows of opportunities for multiple processes
     * to mess with the "as linear" assumptions
     */
    public static void renameFromTo( File pSourceFile, File pDestinationFile )
            throws FileSystemException {
        Confirm.isNotNull( "SourceFile", pSourceFile );
        Confirm.isNotNull( "DestinationFile", pDestinationFile );
        // Win 1 Start
        if ( !pSourceFile.exists() ) {
            throw new FileSystemException( "SourceFile does not exist: " + pSourceFile.getAbsolutePath() );
        }
        if ( pDestinationFile.exists() ) {
            throw new FileSystemException( "DestinationFile already exists: " + pDestinationFile.getAbsolutePath() );
        }
        // Win 2 Start
        if ( !pSourceFile.renameTo( pDestinationFile ) )    // Win 1 End
        {
            throw renameFailed( pSourceFile, pDestinationFile, "Failed" );
        }
        boolean sThere = pSourceFile.exists();
        boolean dThere = pDestinationFile.exists();
        // Win 2 End
        if ( sThere ) {
            throw renameFailed( pSourceFile, pDestinationFile, "claims Success, but Source still there" + (dThere ? " and so is the Destination!" : "!") );
        }
        if ( !dThere ) {
            throw renameFailed( pSourceFile, pDestinationFile, "claims Success, but the Destination is NOT there!" );
        }
    }

    private static FileSystemException renameFailed( File pSourceFile, File pDestinationFile, String pAdditionalExplanation ) {
        throw new FileSystemException(
                "Rename (" + pSourceFile.getAbsolutePath() + ") to (" + pDestinationFile.getAbsolutePath() + ") " + pAdditionalExplanation );
    }

    public static void writeLines( File pFile, boolean pAppend, String... pLines )
            throws FileSystemException {
        addLines( pFile, pAppend, pLines );
    }

    private static void addLines( File pFile, boolean pAppend, String... pLines )
            throws FileSystemException {
        try {
            addLines( createWriter( pFile, pAppend ), pLines );
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    private static void addLines( BufferedWriter pWriter, String... pLines )
            throws IOException {
        boolean closed = false;
        try {
            for ( String line : Strings.deNull( pLines ) ) {
                if ( line != null ) {
                    pWriter.write( line );
                    pWriter.write( '\n' );
                }
            }
            closed = true;
            pWriter.close();
        }
        finally {
            if ( !closed ) {
                Closeables.dispose( pWriter );
            }
        }
    }

    private static boolean areEqual( List<byte[]> pBytes1, List<byte[]> pBytes2 ) {
        if ( byteCount( pBytes1 ) == byteCount( pBytes2 ) ) {
            ByteStream zStream1 = new ByteStream( pBytes1 );
            ByteStream zStream2 = new ByteStream( pBytes2 );
            for ( Byte zByte1; Objects.areNonArraysEqual( zByte1 = zStream1.next(), zStream2.next() ); ) {
                if ( zByte1 == null ) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int byteCount( List<byte[]> pBytes ) {
        int zCount = 0;
        for ( byte[] zBlock : pBytes ) {
            zCount += zBlock.length;
        }
        return zCount;
    }

    private static class ByteStream {
        private final Iterator<byte[]> mIterator;
        private byte[] mCurrentBlock;
        private int mBlockOffset;

        public ByteStream( List<byte[]> pBytes ) {
            mIterator = pBytes.iterator();
            prepBlock();
        }

        private void prepBlock() {
            if ( mIterator.hasNext() ) {
                mCurrentBlock = mIterator.next();
                mBlockOffset = 0;
            } else {
                mCurrentBlock = null;
                mBlockOffset = -1;
            }
        }

        public Byte next() {
            for (; mBlockOffset != -1; prepBlock() ) {
                if ( mBlockOffset < mCurrentBlock.length ) {
                    return mCurrentBlock[mBlockOffset++];
                }
            }
            return null;
        }
    }
}
