package org.litesoft.server.file;

import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.commonfoundation.typeutils.*;

import java.util.*;

public class NonReportingMutations extends AbstractMutations {
    public static synchronized NonReportingMutations get( String pSubstitutionFilesFolder, String pNonReportingType ) {
        NonReportingMutations zMutations = INSTANCES.get( pNonReportingType = pNonReportingType.toLowerCase() );
        if ( zMutations == null ) {
            INSTANCES.put( pNonReportingType, zMutations = new NonReportingMutations( pSubstitutionFilesFolder, pNonReportingType ) );
        }
        return zMutations;
    }

    public String apply( String pText ) {
        for ( SubstitutionPair zPair : mSubstitutionPairs ) {
            pText = zPair.apply( pText );
        }
        return pText;
    }

    private static final Map<String, NonReportingMutations> INSTANCES = Maps.newHashMap();

    private NonReportingMutations( String pFilesFolder, String pNonReportingType ) {
        super( pFilesFolder, "NonReporting." + pNonReportingType );
        if ( mSubstitutionPairs == null ) {
            throw new FileSystemException( "Not Found: " + mFilePath );
        }
    }
}
