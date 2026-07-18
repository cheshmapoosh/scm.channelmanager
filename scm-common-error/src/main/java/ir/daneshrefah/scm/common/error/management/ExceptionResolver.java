package ir.daneshrefah.scm.common.error.management;


import ir.daneshrefah.scm.common.constant.ExceptionResolverLevel;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

@Slf4j
public abstract class ExceptionResolver<E extends Throwable> {


    @PostConstruct
    public void init(){
        ExceptionResolverHelper.getInstance().cacheResolver(this);
        log.info(">>> Exception Resolver : [{}] loaded",this.getClass().getName());
    }

    public abstract List<Error> resolve(E exception, Locale locale);

    /**
     * Resolve an exception using an error code returned by a remote provider.
     * Resolvers that do not need the remote code keep their existing behavior.
     */
    public List<Error> resolve(E exception, Locale locale, String remoteErrorCode) {
        return resolve(exception, locale);
    }

    public List<Error> resolve(Message message, E exception, Locale locale){
        return resolve(exception,locale);
    }

    public ExceptionResolverLevel getResolverLevel(){
        return ExceptionResolverLevel.DEFAULT;
    }

    public Type getExceptionType() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getActualTypeArguments()[0];
        } else {
            throw new IllegalArgumentException(">>> no generic type found for custom exception resolver");
        }
    }
}
