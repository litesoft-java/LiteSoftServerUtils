package org.litesoft.server.util;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;

import java.io.*;

public class IOCopier {
    public static IOCopier from( IOSupplier<InputStream> pISSupplier ) {
        return new IOCopier( Confirm.isNotNull( "InputStream Supplier", pISSupplier ) );
    }

    public void to( IOSupplier<OutputStream> pOSSupplier ) {
        Confirm.isNotNull( "OutputStream Supplier", pOSSupplier );
        // The follow ugly code is to ensure that the Streams are closed appropriately on an error!
        InputStream zInputStream;
        try {
            zInputStream = mISSupplier.get();
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
        OutputStream zOutputStream;
        try {
            zOutputStream = pOSSupplier.get();
        }
        catch ( IOException e ) {
            Closeables.dispose( zInputStream );
            throw new FileSystemException( e );
        }
        try {
            IOUtils.copy( zInputStream, zOutputStream );
        }
        catch ( IOException e ) {
            throw new FileSystemException( e );
        }
    }

    private IOSupplier<InputStream> mISSupplier;

    private IOCopier( IOSupplier<InputStream> pISSupplier ) {
        mISSupplier = pISSupplier;
    }
}
