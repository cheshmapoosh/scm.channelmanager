package ir.daneshrefah.scm.common.error.management;


import ir.daneshrefah.scm.common.constant.ExceptionResolverLevel;
import ir.daneshrefah.scm.common.model.error.Error;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.*;
import java.util.*;

@Slf4j
public class ExceptionResolverHelper {
    private static final ExceptionResolverHelper EXCEPTION_RESOLVER_HELPER = new ExceptionResolverHelper();
    private static final List<ExceptionResolver<?>> ORDERED_RESOLVER_CACHE = new ArrayList<>();

    private ExceptionResolverHelper() {
    }

    public static ExceptionResolverHelper getInstance() {
        return EXCEPTION_RESOLVER_HELPER;
    }

    private boolean isInstance(Object obj, Class<?> clazz) {
        if (obj == null) {
            return false;
        }
        return clazz.isInstance(obj);
    }


    protected void cacheResolver(ExceptionResolver<?> exceptionResolver) {
        synchronized (this) {
            ORDERED_RESOLVER_CACHE.add(exceptionResolver);
            ORDERED_RESOLVER_CACHE.sort(Comparator.comparingInt(o -> o.getResolverLevel().getOrder()));
            log.info(">>> {} exception resolver has been loaded.", exceptionResolver);
        }
    }


    private Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClassFromType(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        } else if (type instanceof TypeVariable<?>) {
            return Object.class;
        } else if (type instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) type).getUpperBounds();
            if (upperBounds.length == 1) {
                return getClassFromType(upperBounds[0]);
            }
        }
        return null;
    }


    private Optional<ExceptionResolver<?>> findResolver(ExceptionResolverLevel resolverLevel) {
        return ORDERED_RESOLVER_CACHE
                .stream()
                .filter(resolver -> resolver.getResolverLevel().equals(resolverLevel))
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    public Error resolve(Throwable throwable, Locale locale) {
        Error error = null;
        for (ExceptionResolver<?> exceptionResolver : ORDERED_RESOLVER_CACHE) {
            if (isInstance(throwable, getClassFromType(exceptionResolver.getExceptionType()))) {
                try {
                    ExceptionResolver<Throwable> resolver = (ExceptionResolver<Throwable>) exceptionResolver;
                    error = resolver.resolve(throwable, locale);
                } catch (Throwable t) {
                    /*If developer resolver throws any un handled exception during resolving the default
                    resolver handled it */
                    ExceptionResolver<Throwable> defaultResolver = (ExceptionResolver<Throwable>) getDefaultResolver();
                    error = defaultResolver.resolve(throwable, locale);
                }
            }
        }
        return error;
    }

    /**
     * Find default resolver by priority level
     */
    private ExceptionResolver<?> getDefaultResolver() {
        int resolverLevel = ExceptionResolverLevel.values().length;
        for (int i = 1; i < resolverLevel; i++) {
            ExceptionResolverLevel level = ExceptionResolverLevel.values()[i];
            Optional<ExceptionResolver<?>> foundDefaultResolver = findResolver(level);
            if (foundDefaultResolver.isPresent()) {
                return foundDefaultResolver.get();
            }
        }
        throw new RuntimeException(">>> There is no any default resolver");
    }


}
