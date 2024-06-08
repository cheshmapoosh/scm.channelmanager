package ir.daneshrefah.scm.common.error.management;


import ir.daneshrefah.scm.common.model.message.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.*;
import java.util.*;

@Slf4j
public class ExceptionResolverHelper {
    private ExceptionResolverHelper(){}
    private static final ExceptionResolverHelper EXCEPTION_RESOLVER_HELPER = new ExceptionResolverHelper();

    public static ExceptionResolverHelper getInstance() {
        return EXCEPTION_RESOLVER_HELPER;
    }

    private static final List<ExceptionResolver<?>> ORDERED_RESOLVER_CACHE =  new ArrayList<>();


    private boolean isInstance(Object obj, Class<?> clazz) {
        if (obj == null) {
            return false;
        }
        return clazz.isInstance(obj);
    }


    protected void cacheResolver(ExceptionResolver<?> exceptionResolver) {
        synchronized (this) {
            ORDERED_RESOLVER_CACHE.add(exceptionResolver);
            List<ExceptionResolver<?>> temp =  new ArrayList<>();
            ORDERED_RESOLVER_CACHE.stream().sorted(Comparator.comparingInt(o -> o.getPriority().getOrder())).forEach(temp::add);
            ORDERED_RESOLVER_CACHE.clear();
            ORDERED_RESOLVER_CACHE.addAll(temp);
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

    @SuppressWarnings("unchecked")
    public void resolveException(Message message, Throwable throwable, Locale locale) {
        for (ExceptionResolver<?> exceptionResolver : ORDERED_RESOLVER_CACHE) {
            if (isInstance(throwable, getClassFromType(exceptionResolver.getExceptionType()))) {
                ExceptionResolver<Throwable> resolver = (ExceptionResolver<Throwable>) exceptionResolver;
                resolver.resolve(Objects.nonNull(message) ? message : Message.builder().build(), throwable,locale);
                return;
            }
        }

    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<?> resolveException(Throwable throwable, Locale locale) {
        for (ExceptionResolver<?> exceptionResolver : ORDERED_RESOLVER_CACHE) {
            if (isInstance(throwable, getClassFromType(exceptionResolver.getExceptionType()))) {
                ExceptionResolver<Throwable> resolver = (ExceptionResolver<Throwable>) exceptionResolver;
                return resolver.resolve(throwable,locale);
            }
        }
        return ResponseEntity.internalServerError().build();
    }


}
