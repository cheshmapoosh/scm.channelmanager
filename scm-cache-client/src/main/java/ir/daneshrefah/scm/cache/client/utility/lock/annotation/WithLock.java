package ir.daneshrefah.scm.cache.client.utility.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithLock {

    String name();

    /**
     * Uses per-user key strategy based on current authenticated principal.
     * If true and {@link #key()} is empty, key becomes methodKey + "::uid::" + username.
     */
    boolean perUser() default false;

    /**
     * Uses one shared key for all callers.
     * If true and {@link #key()} is empty, key becomes "global".
     */
    boolean global() default false;

    /**
     * Optional SpEL key expression. When specified, it overrides other key strategies.
     */
    String key() default "";

    long waitMillis() default 0L;
}
