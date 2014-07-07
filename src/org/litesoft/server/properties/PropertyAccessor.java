package org.litesoft.server.properties;

import java.util.*;

public interface PropertyAccessor extends PropertyRetriever {
    /**
     * Returns the keys in this property list (order is NOT specified).
     * <p/>
     *
     * @return the keys in this property list (order is NOT specified).
     */
    public String[] getPropertyNames();

    /**
     * Return the property value associated with the specified key.
     *
     * @param pKey the property key.
     *
     * @return the value in this property list with the specified key value if Found.
     */
    public String getPropertyRequired( String pKey )
            throws NoSuchElementException;

    /**
     * Return the property value associated with the specified key or the default value if the key is not found.
     *
     * @param pKey          the property key.
     * @param pDefaultValue a default value.
     *
     * @return the value in this property list with the specified key value or the default value.
     */
    public String getProperty( String pKey, String pDefaultValue );
}
