package org.litesoft.server.file;

import org.litesoft.commonfoundation.annotations.*;
import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.issue.*;
import org.litesoft.commonfoundation.typeutils.*;

import java8.util.function.*;

import java.io.*;
import java.util.*;

public class MutationFileLoader {
    private final String mFilesFolder, mSubstitutionFilesFolder;

    public MutationFileLoader( @NotNull String pFilesFolder, @NotNull String pSubstitutionFilesFolder ) {
        mFilesFolder = pFilesFolder;
        mSubstitutionFilesFolder = pSubstitutionFilesFolder;
    }

    public MutationFileLoader( @NotNull String pFilesFolder ) {
        this( pFilesFolder, pFilesFolder + "/../" );
    }

    public @Nullable SourcedContent loadTextFile( @NotNull Source pSource, MutationFileData pFileData ) {
        return loadTextFile( pSource, pFileData, null );
    }

    public @Nullable SourcedContent loadTextFile( @NotNull Source pSource, MutationFileData pFileData, UnaryOperator<String> pInitialMutation ) {
        if ( (pFileData == null) || !pFileData.hasFileName() ) {
            return null;
        }
        String zFileName = pFileData.getFileName();
        String[] zLines = FileUtils.loadTextFile( new File( mFilesFolder, zFileName ) );
        pSource = pSource.plus( "File" ).of( zFileName );
        String zContents = Strings.combine( '\n', zLines );
        if ( pInitialMutation != null ) {
            zContents = pInitialMutation.apply( zContents );
        }
        zContents = applyReplacements( pSource, pFileData, zContents );
        return new SourcedContent( pSource, zContents );
    }

    protected final @NotNull String applyReplacements( @NotNull Source pSource, @NotNull MutationFileData pFileData, @NotNull String pText ) {
        String zFileName = pFileData.getFileName();
        pText = applyAndReportReplacements( pSource, pText,
                                            pFileData.getReplacementsFromTo() );
        pText = applyAndReportReplacements( pSource, pText,
                                            new ReportingMutations( mSubstitutionFilesFolder, zFileName ).getReplacementsFromTo() );

        String zNonReportingType = getFileExtension( zFileName );
        return (zNonReportingType == null) ? pText : NonReportingMutations.get( mSubstitutionFilesFolder, zNonReportingType ).apply( pText );
    }

    protected final String applyAndReportReplacements( @NotNull Source pSource, @NotNull String pText, @NotNull List<SubstitutionPair> pReplacementsFromTo ) {
        for ( SubstitutionPair zPair : pReplacementsFromTo ) {
            String zMutatedText = zPair.apply( pText );
            if ( zMutatedText.equals( pText ) ) { // No Change!
                addWarning( pSource, "NotFound", "'" + zPair.getFrom() + "'" );
            } else {
                pText = zMutatedText;
                addWarning( pSource, "Replaced", "'" + zPair.getFrom() + "' -> '" + zPair.getTo() + "'" );
            }
        }
        return pText;
    }

    private void addWarning( Source pSource, String pAction, String pDetails ) {
        pSource.addWarning( Issue.of( "Replacements", pAction ).with( pDetails ) );
    }

    private String getFileExtension( String pFileName ) {
        int zAt = pFileName.lastIndexOf( '.' );
        return (zAt == -1) ? null : ConstrainTo.significantOrNull( pFileName.substring( zAt + 1 ) );
    }
}
