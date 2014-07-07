package org.litesoft.server.file;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;

public class MutationFileData {
    public static final class ReplacementPair extends SubstitutionPair {
        public ReplacementPair( String pFrom, String pTo ) {
            super( pFrom, pTo );
        }

        public static ReplacementPair quoted( String pFrom, String pTo ) {
            return new ReplacementPair( quote( pFrom ), quote( pTo ) );
        }

        private static String quote( String pString ) {
            return "\"" + pString + "\"";
        }
    }

    private final String mFileName;
    private final List<SubstitutionPair> mReplacementsFromTo;

    private MutationFileData( String pFileName, List<SubstitutionPair> pReplacementsFromTo ) {
        mFileName = ConstrainTo.significantOrNull( pFileName );
        mReplacementsFromTo = Collections.unmodifiableList( pReplacementsFromTo );
    }

    public MutationFileData( String pFileName ) {
        this( pFileName, Lists.<SubstitutionPair>empty() );
    }

    public MutationFileData( String pFileName, String pReplaceFrom, String pReplaceTo, String... pAdditionalReplacementsFromToPairs ) {
        this( pFileName, SubstitutionPair.createListFrom( Maps.newLinkedHashMap( pReplaceFrom, pReplaceTo, pAdditionalReplacementsFromToPairs ) ) );
    }

    public MutationFileData( String pFileName, SubstitutionPair pReplacement, SubstitutionPair... pAdditionalReplacements ) {
        this( pFileName, toList( Confirm.isNotNull( "Replacement", pReplacement ), pAdditionalReplacements ) );
    }

    private static List<SubstitutionPair> toList( SubstitutionPair pReplacement, SubstitutionPair[] pAdditionalReplacements ) {
        return Lists.append( Lists.newArrayList( pReplacement ), pAdditionalReplacements );
    }

    public boolean hasFileName() {
        return (mFileName != null);
    }

    public String getFileName() {
        return mFileName;
    }

    public List<SubstitutionPair> getReplacementsFromTo() {
        return mReplacementsFromTo;
    }
}
