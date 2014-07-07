package org.litesoft.server.dynamicload;

import org.litesoft.commonfoundation.base.*;
import org.litesoft.commonfoundation.exceptions.*;
import org.litesoft.server.properties.*;

import java.lang.reflect.*;

public class ClassForName {
    public static Class<?> loadAnyTypeClass( String pClassName ) {
        return new EmptyConstructorInstanceCreater<Object>().loadClass( null, pClassName );
    }

    public static <T> T newInstance( Class<T> pType, String pClassName ) {
        return new EmptyConstructorInstanceCreater<T>().newInstance( pType, pClassName );
    }

    public static <T> T newInstance( Class<T> pType, PropertyAccessor pPropertyAccessor ) {
        Confirm.isNotNull( "PropertyAccessor", pPropertyAccessor );
        return newInstance( pType, pPropertyAccessor.getPropertyRequired( pType.getSimpleName() ), pPropertyAccessor );
    }

    public static <T> T newInstance( Class<T> pType, Class<? extends T> pDefaultType, PropertyAccessor pPropertyAccessor ) {
        Confirm.isNotNull( "PropertyAccessor", pPropertyAccessor );
        return newInstance( pType, pPropertyAccessor.getProperty( pType.getSimpleName(), pDefaultType.getName() ), pPropertyAccessor );
    }

    public static <T> T newInstance( Class<T> pType, PropertyAccessor pPropertyAccessor, Class<?> pPropertyPrefix ) {
        Confirm.isNotNull( "PropertyPrefix", pPropertyPrefix );
        return newInstance( pType, pPropertyAccessor, pPropertyPrefix.getName() );
    }

    public static <T> T newInstance( Class<T> pType, PropertyAccessor pPropertyAccessor, String pPropertyPrefix ) {
        pPropertyPrefix = Confirm.significant( "PropertyPrefix", pPropertyPrefix );
        Confirm.isNotNull( "PropertyAccessor", pPropertyAccessor );
        return newInstance( pType, pPropertyAccessor.getPropertyRequired( pPropertyPrefix + "-" + pType.getSimpleName() ), pPropertyAccessor );
    }

    public static <T> T newInstance( Class<T> pType, String pClassName, PropertyAccessor pNewInstanceConstructorParam ) {
        if ( pNewInstanceConstructorParam == null ) {
            return newInstance( pType, pClassName );
        }
        return new PropertyAccessorConstructorInstanceCreater<T>( pNewInstanceConstructorParam ).newInstance( pType, pClassName );
    }

    public static <T> T newInstance( Class<T> pType, String pClassName, String pNewInstanceConstructorParam ) {
        if ( pNewInstanceConstructorParam == null ) {
            return newInstance( pType, pClassName );
        }
        return new StringConstructorInstanceCreater<T>( pNewInstanceConstructorParam ).newInstance( pType, pClassName );
    }

    public static <T> Class<T> requiredOfType( Class<T> pType, String pClassName )
            throws ClassNotFoundException {
        ClassLoader zClassLoader = pType.getClassLoader();
        if ( zClassLoader == null ) {
            zClassLoader = ClassForName.class.getClassLoader();
        }
        Class<?> zClass = zClassLoader.loadClass( pClassName );
        if ( pType.isAssignableFrom( zClass ) ) {
            return Cast.tryClass( zClass );
        }
        throw new Error( pType.getName() + " NOT implemented by " + pClassName );
    }

    private static abstract class InstanceCreater<T> {
        public Class<?> loadClass( ClassLoader pClassLoader, String pClassName ) {
            try {
                return deNull( pClassLoader ).loadClass( pClassName );
            }
            catch ( ClassNotFoundException e ) {
                throw new InternalServerErrorException( "Unable to locate: " + pClassName );
            }
        }

        public T newInstance( Class<T> pType, String pClassName ) {
            Confirm.isNotNull( "Type", pType );
            Confirm.isNotNull( "ClassName", pClassName );
            try {
                T rv = LLnewInstance( pType, pClassName );
                if ( rv instanceof Singletonable ) {
                    rv = Cast.tryObject( pType, ((Singletonable) rv).getSingleton() );
                }
                if ( rv instanceof Initializable ) {
                    ((Initializable) rv).initialize( getPropertyAccessor() );
                }
                return rv;
            }
            catch ( NoSuchMethodException e ) {
                throw new InternalServerErrorException( "No Appropriate Constructor: " + pClassName, e );
            }
            catch ( InstantiationException e ) {
                throw new InternalServerErrorException( "Unable to Instantiate: " + pClassName, e );
            }
            catch ( IllegalAccessException e ) {
                throw new InternalServerErrorException( "Unable to Access: " + pClassName, e );
            }
            catch ( InvocationTargetException e ) {
                throw new InternalServerErrorException( "Constructor errored: " + pClassName, e );
            }
        }

        private T LLnewInstance( Class<T> pType, String pClassName )
                throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            ClassLoader zClassLoader = deNull( pType.getClassLoader() );
            Class<?> zClass = loadClass( zClassLoader, pClassName );
            if ( pType.isAssignableFrom( zClass ) ) {
                Class<T> zNew = Cast.tryClass( zClass );
                return newInstance( zNew );
            }
            if ( !InvocationHandler.class.isAssignableFrom( zClass ) ) {
                throw new InternalServerErrorException( "Inappropriate class type: " + pClassName );
            }
            Class<InvocationHandler> zNew = Cast.tryClass( zClass );
            InvocationHandler zHandler = zNew.newInstance();
            if ( zHandler instanceof InitializableInvocationHandler ) {
                ((InitializableInvocationHandler) zHandler).initialize( pType, getPropertyAccessor() );
            } else if ( zHandler instanceof Initializable ) {
                ((Initializable) zHandler).initialize( getPropertyAccessor() );
            }
            return Cast.tryObject( pType, Proxy.newProxyInstance( zClassLoader, new Class[]{pType}, zHandler ) );
        }

        private ClassLoader deNull( ClassLoader pClassLoader ) {
            return (pClassLoader != null) ? pClassLoader : this.getClass().getClassLoader();
        }

        protected PropertyAccessor getPropertyAccessor() {
            return null;
        }

        abstract protected T newInstance( Class<T> pType )
                throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException;
    }

    private static class EmptyConstructorInstanceCreater<T> extends InstanceCreater<T> {
        @Override
        protected T newInstance( Class<T> pType )
                throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            return pType.newInstance();
        }
    }

    private static class StringConstructorInstanceCreater<T> extends InstanceCreater<T> {
        private String mNewInstanceConstructorParam;

        public StringConstructorInstanceCreater( String pNewInstanceConstructorParam ) {
            mNewInstanceConstructorParam = pNewInstanceConstructorParam;
        }

        @Override
        protected T newInstance( Class<T> pType )
                throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            try {
                Constructor<T> zConstructor = pType.getConstructor( String.class );
                return zConstructor.newInstance( mNewInstanceConstructorParam );
            }
            catch ( NoSuchMethodException e ) {
                return pType.newInstance();
            }
        }
    }

    private static class PropertyAccessorConstructorInstanceCreater<T> extends InstanceCreater<T> {
        private PropertyAccessor mNewInstanceConstructorParam;

        public PropertyAccessorConstructorInstanceCreater( PropertyAccessor pNewInstanceConstructorParam ) {
            mNewInstanceConstructorParam = pNewInstanceConstructorParam;
        }

        @Override
        protected PropertyAccessor getPropertyAccessor() {
            return mNewInstanceConstructorParam;
        }

        @Override
        protected T newInstance( Class<T> pType )
                throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
            try {
                Constructor<T> zConstructor = pType.getConstructor( PropertyAccessor.class );
                return zConstructor.newInstance( mNewInstanceConstructorParam );
            }
            catch ( NoSuchMethodException e ) {
                return pType.newInstance();
            }
        }
    }
}
