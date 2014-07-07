package org.litesoft.server.properties;

import org.litesoft.commonfoundation.base.*;

import java.util.*;

public class MapPropertyAccessor extends AbstractPropertyAccessor {
    private Map<String, String> mProperties = new HashMap<String, String>();

    public MapPropertyAccessor( Map<String, String> pProperties ) {
        if ( pProperties != null ) {
            mProperties.putAll( pProperties );
        }
    }

    public MapPropertyAccessor( String... pNameValuePairs ) {
        if ( pNameValuePairs != null ) {
            if ( (pNameValuePairs.length & 1) == 1 ) // Odd?
            {
                throw new IllegalArgumentException( "Name Value Pairs NOT Paired" );
            }
            for ( int i = 0; i < pNameValuePairs.length; ) {
                String zName = Confirm.significant( "Name", pNameValuePairs[i++] );
                String zValue = Confirm.significant( "Value", pNameValuePairs[i++] );
                mProperties.put( zName, zValue );
            }
        }
    }

    @Override
    public String[] getPropertyNames() {
        return mProperties.keySet().toArray( new String[mProperties.size()] );
    }

    @Override
    protected String getValueForNonNull( String pKey ) {
        return mProperties.get( pKey );
    }
}
