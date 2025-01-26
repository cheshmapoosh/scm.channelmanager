package ir.daneshrefah.scm.common.annotation;

import ir.daneshrefah.scm.common.constant.JavaMethodType;
import ir.daneshrefah.scm.common.constant.ServiceCode;
import ir.daneshrefah.scm.common.constant.Status;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-12
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JavaService {

    ServiceCode serviceCode();

    /**
     * @return Default as serviceCode
     */
    String title() default "";

    String path() default "";

    /**
     * @return Default INQUIRY (GET) , PARENT DOES NOT SUPPORTED
     */
    JavaMethodType type() default JavaMethodType.NULL;

    ServiceCode parentCode() default ServiceCode.NULL;

    Status checkAccessFirstAuthentication() default Status.DE_ACTIVE;

    Status checkAccessSecondAuthentication() default Status.DE_ACTIVE;

    Status checkAccessService() default Status.DE_ACTIVE;

    Status checkAccessAsset() default Status.DE_ACTIVE;

}
