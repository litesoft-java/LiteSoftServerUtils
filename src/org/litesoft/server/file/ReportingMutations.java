package org.litesoft.server.file;

import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;

public class ReportingMutations extends AbstractMutations {

    public ReportingMutations( String pSubstitutionFilesFolder, String pFileName ) {
        super( pSubstitutionFilesFolder, "-" + pFileName );
    }

    public List<SubstitutionPair> getReplacementsFromTo() {
        return Lists.deNullUnmodifiable( mSubstitutionPairs );
    }
}
