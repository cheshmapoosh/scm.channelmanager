package ir.daneshrefah.scm.common.error.management;


import ir.daneshrefah.scm.common.constant.ExceptionResolverLevel;
import ir.daneshrefah.scm.common.model.message.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Locale;

@Slf4j
public abstract class ExceptionResolver<E extends Throwable> {


    @PostConstruct
    public void init(){
        ExceptionResolverHelper.getInstance().cacheResolver(this);
        log.info(">>> Exception Resolver : [{}] loaded",this.getClass().getName());
    }

    public abstract void resolve(Message message, E exception, Locale locale);
    public abstract ResponseEntity<?> resolve(E exception, Locale locale);
    public ExceptionResolverLevel getPriority(){
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
