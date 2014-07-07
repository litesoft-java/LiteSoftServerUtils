package org.litesoft.server.dynamicload;

import org.litesoft.server.properties.*;

import java.lang.reflect.*;

public interface InitializableInvocationHandler extends InvocationHandler {
    public void initialize( Class<?> pForType, PropertyAccessor pPropertyAccessor );
}
