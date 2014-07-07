package org.litesoft.server.file;

import java.io.*;
import java.util.*;

public class AbstractMutations {
    protected static final String SOURCE_PREFIX = "Subs";
    protected static final String SOURCE_SUFFIX = ".txt";

    protected final String mFilePath;
    protected final List<SubstitutionPair> mSubstitutionPairs;

    protected AbstractMutations( String pSubstitutionFilesFolder, String pSourceMiddle ) {
        String zFileName = SOURCE_PREFIX + pSourceMiddle + SOURCE_SUFFIX;
        File zFile = new File( pSubstitutionFilesFolder + zFileName );
        mFilePath = zFile.getAbsolutePath();
        mSubstitutionPairs = !zFile.isFile() ? null : SubstitutionPair.createListFrom( zFileName, FileUtils.loadTextFile( zFile ) );
    }
}
