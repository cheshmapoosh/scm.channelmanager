package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
@RequiredArgsConstructor
public class UaaLegacyBusinessObservationAspect {
    private final UaaObservation observation;

    @Around("execution(public * ir.daneshrefah.scm.uaa.controller.user.UserController.changePassword(..))")
    public Object observeChangePassword(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "change-password", "uaa.user.change-password");
    }

    @Around("execution(public * ir.daneshrefah.scm.uaa..*.updateFavoriteAccount(..))")
    public Object observeUpdateFavoriteAccount(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "update-favorite-account", "uaa.user.update-favorite-account");
    }

    @Around("execution(public * ir.daneshrefah.scm.uaa..*.updateAccountLabel(..))")
    public Object observeUpdateAccountLabel(ProceedingJoinPoint joinPoint) throws Throwable {
        return observe(joinPoint, "update-account-label", "uaa.user.update-account-label");
    }

    private Object observe(ProceedingJoinPoint joinPoint, String operationCode, String spanName) throws Throwable {
        ObservationScope scope = observation.traceLegacyBusinessOperation(operationCode, spanName, channelCode(joinPoint));
        try {
            Object result = joinPoint.proceed();
            scope.success();
            return result;
        } catch (Throwable ex) {
            scope.failure(ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    private String channelCode(ProceedingJoinPoint joinPoint) {
        if (joinPoint == null || joinPoint.getArgs() == null) {
            return null;
        }
        for (Object arg : joinPoint.getArgs()) {
            String value = readStringGetter(arg, "getChannelCode");
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String readStringGetter(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            Object value = method.invoke(target);
            return value == null || String.valueOf(value).isBlank() ? null : String.valueOf(value).trim();
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}
