package org.litesoft.server.properties;

import java.util.*;

public abstract class AbstractPropertyAccessor implements PropertyAccessor {
    abstract protected String getValueForNonNull( String pKey );

    @Override
    public final String getProperty( String pKey ) {
        return (pKey != null) ? getValueForNonNull( pKey ) : null;
    }

    @Override
    public String getPropertyRequired( String pKey )
            throws NoSuchElementException {
        String zValue = getProperty( pKey );
        if ( zValue != null ) {
            return zValue;
        }
        throw new NoSuchElementException( "Missing Property: " + pKey );
    }

    @Override
    public final String getProperty( String pKey, String pDefaultValue ) {
        String zValue = getProperty( pKey );
        return (zValue != null) ? zValue : pDefaultValue;
    }
}
