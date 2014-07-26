package org.litesoft.server.util;

import org.litesoft.commonfoundation.exceptions.*;

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

    public static void close( Closeable pCloseable ) {
        if ( pCloseable != null ) {
            try {
                pCloseable.close();
            }
            catch ( IOException e ) {
                throw new WrappedIOException( e );
            }
        }
    }
}
