package org.litesoft.server.properties;

public class OverridablePropertyAccessor extends AbstractPropertyAccessor {
    private PropertyAccessor mProxy;

    public OverridablePropertyAccessor( PropertyAccessor pProxy ) {
        mProxy = pProxy;
    }

    @Override
    public String[] getPropertyNames() {
        return (mProxy != null) ? mProxy.getPropertyNames() : new String[0];
    }

    @Override
    protected String getValueForNonNull( String pKey ) {
        String zValue = System.getProperty( pKey ); //. . . . . . . . . . . 1st -D (command line switch)
        if ( zValue != null ) {
            return zValue;
        }
        try {
            if ( null != (zValue = System.getenv( pKey )) ) //. . . . . . . . 2nd Environment
            {
                return zValue;
            }
        }
        catch ( RuntimeException e ) {
            // Assume a Security Exception, so skip the Environment Level
        }
        return (mProxy != null) ? mProxy.getProperty( pKey ) : null; // . . 3rd Passed in PropertyAccessor
    }
}
