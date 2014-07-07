package org.litesoft.server.properties;

public interface PropertyRetriever {
    /**
     * Return the property value associated with the specified key.
     * The method returns <code>null</code> if the property is not found.
     *
     * @param pKey the property key.
     *
     * @return the value in this property list with the specified key value or <code>null</code>.
     */
    public String getProperty( String pKey );
}
