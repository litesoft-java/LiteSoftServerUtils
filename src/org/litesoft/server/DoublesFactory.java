package org.litesoft.server;

import org.litesoft.commonfoundation.typeutils.*;

import java.text.*;
import java.util.*;

public class DoublesFactory extends Doubles.Factory {
    private final Map<String, Doubles> mDoublesByPatterns = Maps.newHashMap();

    @Override
    public Doubles getFormat( String pPattern ) {
        Doubles zDoubles = mDoublesByPatterns.get( pPattern );
        if ( zDoubles == null ) {
            mDoublesByPatterns.put( pPattern, zDoubles = new Regular_Doubles( pPattern ) );
        }
        return zDoubles;
    }

    private static class Regular_Doubles extends Doubles {
        public final DecimalFormat mFormatter;

        private Regular_Doubles( String pPattern ) {
            mFormatter = new DecimalFormat( pPattern );
        }

        /**
         * As this is not expected to be used much on the Server side, the choice to synchronize on each call vs
         * creating a new pattern on each call was chosen!
         */
        @Override
        public synchronized String format( double pValue ) {
            return mFormatter.format( pValue );
        }
    }
}
