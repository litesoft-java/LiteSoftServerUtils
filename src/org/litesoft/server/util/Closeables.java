package org.litesoft.server.util;

import java.io.*;

public class Closeables {
    public static void dispose( Closeable pCloseable ) {
        if ( pCloseable != null ) {
            try {
                pCloseable.close();
            }
            catch ( IOException e ) {
                // Whatever
            }
        }
    }
}
