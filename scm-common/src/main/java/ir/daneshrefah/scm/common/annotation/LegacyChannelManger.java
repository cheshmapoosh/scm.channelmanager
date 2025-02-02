package ir.daneshrefah.scm.common.annotation;

import java.lang.annotation.*;

/**
 * Annotated classes and methods that using legacy channel manger business.
 * notice the annotated methods will be deleted and replaced with SCM structures in futures, so
 * do not use annotated method in any part of application.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LegacyChannelManger {
}
