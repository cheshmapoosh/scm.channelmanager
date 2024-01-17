package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotDefinedException;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotFoundException;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceExecutionException;
import ir.daneshrefah.scm.plugin.api.exception.JavaServiceMethodNotFoundException;
import ir.daneshrefah.scm.plugin.api.model.service.java.JavaService;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-07
 */
@Service
public class JavaServiceExecutor extends ServiceExecutor {

    @Override
    protected Object executeInternal(ir.daneshrefah.scm.common.model.service.Service service, Message message, Object requestPayload) {
        JavaService javaService = (JavaService) service;
        String classNameOrg = javaService.getJavaImplementationClassName();
        String className = classNameOrg;
        String methodName = null;
        if (StringUtils.isEmpty(classNameOrg)) {
            throw new JavaServiceClassNotDefinedException(javaService);
        }
        if (classNameOrg.contains(".")) {
            className = classNameOrg.split("\\.")[0];
            methodName = classNameOrg.split("\\.")[1];
        }
        AbstractJavaService javaServiceInstance = null;
        try {
            javaServiceInstance = ClassLoader.findBeanOrCreateInstanceOfClass(className, AbstractJavaService.class);
        } catch (Exception e) {
            throw new JavaServiceClassNotFoundException(e, javaService);
        }

        try {
            if (StringUtils.isEmpty(methodName)) {
                return javaServiceInstance.execute(message, service, requestPayload);
            }
        } catch (Exception e) {
            throw new JavaServiceExecutionException(e, javaService);
        }


        Method method = null;
        try {
            method = javaServiceInstance.getClass().getMethod(methodName, Message.class,
                    ir.daneshrefah.scm.common.model.service.Service.class, Object.class);
            return method.invoke(javaServiceInstance, message, service, requestPayload);
        } catch (NoSuchMethodException e) {
            throw new JavaServiceMethodNotFoundException(e, javaService);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new JavaServiceExecutionException(e, javaService);
        }
    }
}
