package org.litesoft.server.properties;

import org.litesoft.commonfoundation.base.*;

import java.util.*;

public class PropertiesPropertyAccessor extends AbstractPropertyAccessor {
    private Properties mProperties;

    public PropertiesPropertyAccessor( Properties pProperties ) {
        mProperties = Confirm.isNotNull( "Properties", pProperties );
    }

    @Override
    public String[] getPropertyNames() {
        Set<String> zSet = mProperties.stringPropertyNames();
        return zSet.toArray( new String[zSet.size()] );
    }

    @Override
    protected String getValueForNonNull( String pKey ) {
        return mProperties.getProperty( pKey );
    }
}
