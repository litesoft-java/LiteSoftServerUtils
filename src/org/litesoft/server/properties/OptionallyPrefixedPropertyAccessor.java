package org.litesoft.server.properties;

import org.litesoft.commonfoundation.base.*;

import java.util.*;

public class OptionallyPrefixedPropertyAccessor extends AbstractPropertyAccessor {
    private final PropertyAccessor mProxy;
    private final String[] mPrefixes;

    public OptionallyPrefixedPropertyAccessor( PropertyAccessor pProxy, String... pPrefixes ) {
        mProxy = pProxy;
        List<String> zPrefixes = new ArrayList<String>();
        if ( pPrefixes != null ) {
            for ( String zPrefix : pPrefixes ) {
                if ( null != (zPrefix = ConstrainTo.significantOrNull( zPrefix )) ) {
                    if ( (zPrefix.indexOf( ' ' ) != -1) || (zPrefix.indexOf( ':' ) != -1) ) {
                        throw new IllegalArgumentException( "Prefixes can not contain spaces or colons: '" + zPrefix + "'" );
                    }
                    if ( !zPrefix.endsWith( "-" ) ) {
                        zPrefix += "-";
                    }
                    zPrefixes.add( zPrefix );
                }
            }
        }
        mPrefixes = zPrefixes.toArray( new String[zPrefixes.size()] );
    }

    @Override
    public String[] getPropertyNames() {
        return (mProxy != null) ? mProxy.getPropertyNames() : new String[0];
    }

    @Override
    protected String getValueForNonNull( String pKey ) {
        for ( String zPrefix : mPrefixes ) {
            String zValue = LLgetProperty( zPrefix + pKey );
            if ( zValue != null ) {
                return zValue;
            }
        }
        return LLgetProperty( pKey );
    }

    private String LLgetProperty( String pKey ) {
        return (mProxy != null) ? mProxy.getProperty( pKey ) : null;
    }
}
