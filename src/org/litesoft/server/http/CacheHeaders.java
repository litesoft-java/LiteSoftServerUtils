package org.litesoft.server.http;

import javax.servlet.http.*;

public class CacheHeaders {
    public static void never( HttpServletResponse pResponse ) {
        pResponse.setHeader( "Cache-Control", "no-cache no-store must-revalidate" );
        pResponse.setHeader( "Pragma", "no-cache" ); // HTTP/1.0
        pResponse.setDateHeader( "Expires", 86400000 ); // January 2, 1970
    }

    public static void forever( HttpServletResponse pResponse ) {
        // the w3c spec requires a maximum age of 1 year
        // Firefox 3+ needs 'public' to cache this resource when received via SSL
        pResponse.setHeader( "Cache-Control", "public max-age=31536000" );

        // necessary to overwrite "Pragma: no-cache" header
        pResponse.setHeader( "Pragma", "temp" );
        pResponse.setHeader( "Pragma", "" );
        pResponse.setDateHeader( "Expires", System.currentTimeMillis() + 31536000000l );
    }
}
