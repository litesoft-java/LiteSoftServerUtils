package org.litesoft.server.util;

import org.litesoft.commonfoundation.exceptions.*;

@SuppressWarnings("UnusedDeclaration")
public class AccessDeniedFileSystemException extends FileSystemException {
    public AccessDeniedFileSystemException( String message ) {
        super( message );
    }

    public AccessDeniedFileSystemException( String message, Throwable cause ) {
        super( message, cause );
    }

    public AccessDeniedFileSystemException( Throwable cause ) {
        super( cause );
    }
}
