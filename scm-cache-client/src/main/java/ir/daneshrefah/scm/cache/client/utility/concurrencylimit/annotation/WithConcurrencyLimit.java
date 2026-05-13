package ir.daneshrefah.scm.cache.client.utility.concurrencylimit.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithConcurrencyLimit {

    String name();

    int maxConcurrentExecutions() default 1;

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

    /**
     * Wait behavior:
     * - negative: block until a slot is available
     * - zero: immediate tryAcquire (no wait)
     * - positive: wait up to this duration
     */
    long waitMillis() default -1L;
}
