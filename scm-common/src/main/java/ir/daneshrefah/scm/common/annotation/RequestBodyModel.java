package ir.daneshrefah.scm.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Using for Java Service Request Model Definition
 *
 * @author d.abdollahi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RequestBodyModel {
    /**
     * Model class name, default name is Class name.
     */
    String name() default "";
}
