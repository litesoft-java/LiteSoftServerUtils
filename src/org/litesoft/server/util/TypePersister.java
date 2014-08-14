package org.litesoft.server.util;

import org.litesoft.commonfoundation.annotations.*;

public interface TypePersister<T> {
    void save( @NotNull T pInstance );
}
