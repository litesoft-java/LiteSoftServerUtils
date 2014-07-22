package org.litesoft.server.util;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.stringmatching.*;
import org.litesoft.commonfoundation.typeutils.*;
import org.litesoft.commonfoundation.typeutils.proxies.*;

import java8.util.function.*;

import java.io.*;
import java.util.*;

public class ArgsToMap {
    public static class Entry extends NameValuePair {
        private final boolean mKeyed;

        public Entry( String pName, String pValue, boolean pKeyed ) {
            super( pName, pValue );
            mKeyed = pKeyed;
        }

        public boolean isKeyed() {
            return mKeyed;
        }
    }

    private final Map<String, Entry> mEntries = Maps.newLinkedHashMap(); // Preserve Order

    public ArgsToMap( String[] args ) {
        for ( String zArg : args ) {
            String zKey, zValue;
            boolean zKeyed = false;
            int zAt = zArg.indexOf( '=' );
            if ( zAt == -1 ) {
                zKey = keyPrep( zValue = ConstrainTo.significantOrNull( zArg ) );
            } else {
                zKeyed = true;
                zKey = keyPrep( zArg.substring( 0, zAt ) );
                zValue = ConstrainTo.significantOrNull( zArg.substring( zAt + 1 ) );
            }
            if ( (zKey != null) && (zValue != null) ) {
                Entry zPreviousEntry = mEntries.put( zKey, new Entry( zKey, zValue, zKeyed ) );
                if ( zPreviousEntry != null ) {
                    throw new IllegalArgumentException( "Duplicate Entry (key='" + zKey + "'):"
                                                        + "\n   1st Value: " + zPreviousEntry.getValue()
                                                        + "\n   2nd Value: " + zValue );
                }
            }
        }
    }

    private String keyPrep( String pKey ) {
        return (pKey == null) ? null : ConstrainTo.significantOrNull( pKey.toLowerCase() );
    }

    public Entry[] getEntries() {
        return mEntries.values().toArray( new Entry[mEntries.size()] );
    }

    public String[] getValues() {
        Entry[] zEntries = getEntries();
        String[] zValues = new String[zEntries.length];
        for ( int i = 0; i < zEntries.length; i++ ) {
            zValues[i] = zEntries[i].getValue();
        }
        return zValues;
    }

    public List<String> getRemainingKeyedKeys() {
        ArrayList<String> zKeys = Lists.newArrayList();
        for ( Entry zEntry : getEntries() ) {
            if ( zEntry.isKeyed() ) {
                zKeys.add( zEntry.getName() );
            }
        }
        return zKeys;
    }

    public void reportRemainingKeyedKeys(String pWhat, PrintStream pPrintStream) {
        List<String> zRemaining = getRemainingKeyedKeys();
        if ( !zRemaining.isEmpty() ) {
            pPrintStream.println(pWhat + " Arguments: " + zRemaining );
        }
    }

    public List<String> getRemainingNonKeyed() {
        ArrayList<String> zValues = Lists.newArrayList();
        for ( Entry zEntry : getEntries() ) {
            if ( !zEntry.isKeyed() ) {
                zValues.add( zEntry.getValue() );
            }
        }
        return zValues;
    }

    public void assertNoRemainingNonKeyed() {
        List<String> zRemaining = getRemainingNonKeyed();
        if ( !zRemaining.isEmpty() ) {
            throw new IllegalArgumentException( "Unexpected Arguments: " + zRemaining );
        }
    }

    public String getNonKeyed( Supplier<String> pDefaultSupplier ) {
        return orDefault( getNonKeyed(), pDefaultSupplier );
    }

    public String getNonKeyed( String pDefault ) {
        return orDefault( getNonKeyed(), pDefault );
    }

    public String getNonKeyed() {
        for ( Entry zEntry : getEntries() ) {
            if ( !zEntry.isKeyed() ) {
                return get( zEntry.getName() );
            }
        }
        return null;
    }

    public String get( String pKey, Supplier<String> pDefaultSupplier ) {
        return orDefault( get( pKey ), pDefaultSupplier );
    }

    public String get( String pKey, String pDefault ) {
        return orDefault( get( pKey ), pDefault );
    }

    public String get( String pKey ) {
        Entry zEntry = mEntries.remove( keyPrep( pKey ) );
        return (zEntry == null) ? null : zEntry.getValue();
    }

    public String getWithPermutations( String pKey ) {
        if ( null != (pKey = keyPrep( pKey )) ) {
            PermutationMatcher zMatcher = new PermutationMatcher( pKey );
            for ( Entry zEntry : getEntries() ) {
                if ( zEntry.isKeyed() && zMatcher.matches( zEntry.getName() ) ) {
                    return get( zEntry.getName() );
                }
            }
        }
        return null;
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

    private String orDefault( String pValue, String pDefault ) {
        return (pValue != null) ? pValue : pDefault;
    }

    private String orDefault( String pValue, Supplier<String> pDefaultSupplier ) {
        return (pValue != null) ? pValue : pDefaultSupplier.get();
    }
}
