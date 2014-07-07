package org.litesoft.server.util;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.commonfoundation.typeutils.proxies.*;

import java.util.*;

public class ArgsToMap {
    private final Map<String, String> mEntries = Maps.newHashMap();

    public ArgsToMap( String[] args ) {
        for ( String zArg : args ) {
            String zKey, zValue;
            int zAt = zArg.indexOf( '=' );
            if ( zAt == -1 ) {
                zKey = keyPrep( zValue = ConstrainTo.significantOrNull( zArg ) );
            } else {
                zKey = keyPrep( zArg.substring( 0, zAt ) );
                zValue = ConstrainTo.significantOrNull( zArg.substring( zAt + 1 ) );
            }
            if ( (zKey != null) && (zValue != null) ) {
                mEntries.put( zKey, zValue );
            }
        }
    }

    private String keyPrep( String pKey ) {
        return (pKey == null) ? null : ConstrainTo.significantOrNull( pKey.toLowerCase() );
    }

    public String[] getValues() {
        return mEntries.values().toArray( new String[mEntries.size()] );
    }

    public String get( String pKey, String pDefault ) {
        String zValue = get( pKey );
        return (zValue != null) ? zValue : pDefault;
    }

    public String get( String pKey ) {
        return mEntries.remove( keyPrep( pKey ) );
    }

    public String getAnyOf( String pKey, String pDefault, Object... pToStringKeys ) {
        String zValue = get( pKey );
        if ( zValue != null ) {
            return zValue;
        }
        if ( pToStringKeys != null ) {
            for ( Object zKey : pToStringKeys ) {
                if ( null != (zValue = get( zKey.toString() )) ) {
                    return zValue;
                }
                if ( zKey instanceof AlternateNamesAccessable ) {
                    for ( String zAlternateName : ConstrainTo.notNull( ((AlternateNamesAccessable) zKey).getAlternateNames() ) ) {
                        if ( null != (zValue = get( zAlternateName )) ) {
                            return zValue;
                        }
                    }
                }
            }
        }
        return pDefault;
    }
}
