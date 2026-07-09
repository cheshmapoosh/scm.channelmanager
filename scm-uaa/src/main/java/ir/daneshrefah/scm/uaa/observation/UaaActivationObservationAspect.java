package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.starter.ObservationScope;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 30)
@RequiredArgsConstructor
public class UaaActivationObservationAspect {
    private final UaaObservation observation;

    @Around("""
            execution(public * ir.daneshrefah.scm.uaa.service.activation.pwa..*(..)) ||
            execution(public * ir.daneshrefah.scm.uaa.service.activation.nib..*(..))
            """)
    public Object observeActivationFlow(ProceedingJoinPoint joinPoint) throws Throwable {
        UaaObservation.OperationContext ctx = operationContext(joinPoint);
        UaaObservation.OtpContext otpContext = otpContext(ctx);

        ObservationScope scope = traceScope(joinPoint, ctx, otpContext);
        logStarted(ctx, otpContext);
        try {
            Object result = joinPoint.proceed();
            UaaObservation.OperationContext completed = ctx.withResult("success");
            observation.operationAttributes(scope, completed);
            scope.success();
            logCompleted(joinPoint, completed, otpContext == null ? null : otpContext.withResult("success"));
            return result;
        } catch (Throwable ex) {
            String reason = observation.safeErrorMessage(ex);
            UaaObservation.OperationContext failed = ctx.withResult("failure").withFailureReason(reason);
            observation.operationAttributes(scope, failed);
            scope.failure(ex);
            logFailed(failed, otpContext == null ? null : otpContext.withResult("failure").withFailureReason(reason), ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    private ObservationScope traceScope(
            ProceedingJoinPoint joinPoint,
            UaaObservation.OperationContext ctx,
            UaaObservation.OtpContext otpContext
    ) {
        if (isMqOtpPublish(joinPoint)) {
            return observation.traceMqOtpPublish(new UaaObservation.MessagingContext(
                    "queue",
                    "user-activation",
                    "notification",
                    "activation"
            ));
        }
        if (isOtpSend(joinPoint)) {
            return observation.traceOtpSend(otpContext);
        }
        if (isOtpVerify(joinPoint)) {
            return observation.traceOtpVerify(otpContext);
        }
        if (isOtpBlockCheck(joinPoint)) {
            return observation.traceOtpBlockCheck(otpContext);
        }
        if (isOtpTrialUpdate(joinPoint)) {
            return observation.traceOtpTrialUpdate(otpContext);
        }
        return isNib(joinPoint) ? observation.traceActivationNib(ctx) : observation.traceActivationPwa(ctx);
    }

    private void logStarted(UaaObservation.OperationContext ctx, UaaObservation.OtpContext otpContext) {
        if (isOtpVerifySpan(ctx)) {
            observation.otpVerifyStarted(otpContext);
        } else {
            observation.operationStarted(ctx);
        }
    }

    private void logCompleted(
            ProceedingJoinPoint joinPoint,
            UaaObservation.OperationContext ctx,
            UaaObservation.OtpContext otpContext
    ) {
        if (isOtpSend(joinPoint)) {
            observation.otpSent(otpContext);
        } else if (isOtpVerifySpan(ctx)) {
            observation.otpVerifyCompleted(otpContext);
        } else {
            observation.operationCompleted(ctx);
        }
    }

    private void logFailed(
            UaaObservation.OperationContext ctx,
            UaaObservation.OtpContext otpContext,
            Throwable throwable
    ) {
        if (isOtpVerifySpan(ctx)) {
            observation.otpVerifyFailed(otpContext, throwable);
        } else {
            observation.operationFailed(ctx, throwable);
        }
    }

    private UaaObservation.OperationContext operationContext(ProceedingJoinPoint joinPoint) {
        String category = isNib(joinPoint) ? "uaa.activation.nib" : "uaa.activation.pwa";
        return new UaaObservation.OperationContext(
                spanName(joinPoint),
                category,
                step(joinPoint),
                "started",
                null
        );
    }

    private UaaObservation.OtpContext otpContext(UaaObservation.OperationContext ctx) {
        if (ctx == null) {
            return null;
        }
        return new UaaObservation.OtpContext(
                "notification",
                ctx.category(),
                ctx.step(),
                ctx.result(),
                ctx.failureReason()
        );
    }

    private String spanName(ProceedingJoinPoint joinPoint) {
        if (isOtpSend(joinPoint)) {
            return "uaa.otp.send";
        }
        if (isOtpVerify(joinPoint)) {
            return "uaa.otp.verify";
        }
        if (isOtpBlockCheck(joinPoint)) {
            return "uaa.otp.block.check";
        }
        if (isOtpTrialUpdate(joinPoint)) {
            return "uaa.otp.trial.update";
        }
        if (isMqOtpPublish(joinPoint)) {
            return "uaa.mq.otp.publish";
        }

        String method = methodName(joinPoint);
        String type = typeName(joinPoint);
        if (isNib(joinPoint)) {
            if ("activate".equals(method)) {
                return "uaa.activation.nib.request";
            }
            if (method.toLowerCase(Locale.ROOT).contains("check")) {
                return "uaa.activation.nib.verify";
            }
            if (type.contains("Publisher") || type.contains("Subscriber") || type.contains("Notifier")) {
                return "uaa.activation.nib.register.save";
            }
            return "uaa.activation.nib." + normalize(method);
        }

        if ("activationRequest".equals(method)) {
            return "uaa.activation.pwa.request";
        }
        if ("verificationRequest".equals(method) || "checkActivationIfNeeded".equals(method)) {
            return "uaa.activation.pwa.verify";
        }
        if ("save".equals(method) || "saveRegistry".equals(method) || "updateActivationStatus".equals(method)) {
            return "uaa.activation.pwa.register.save";
        }
        return "uaa.activation.pwa." + normalize(method);
    }

    private String step(ProceedingJoinPoint joinPoint) {
        return typeName(joinPoint) + "." + methodName(joinPoint);
    }

    private boolean isOtpSend(ProceedingJoinPoint joinPoint) {
        return typeName(joinPoint).contains("NotificationCenter") && methodName(joinPoint).startsWith("sendActivationOtp");
    }

    private boolean isOtpVerify(ProceedingJoinPoint joinPoint) {
        String method = methodName(joinPoint);
        return method.toLowerCase(Locale.ROOT).contains("verify")
                || "validateActivationCode".equals(method);
    }

    private boolean isOtpBlockCheck(ProceedingJoinPoint joinPoint) {
        String method = methodName(joinPoint).toLowerCase(Locale.ROOT);
        return method.contains("blocked") || method.contains("block");
    }

    private boolean isOtpTrialUpdate(ProceedingJoinPoint joinPoint) {
        String method = methodName(joinPoint).toLowerCase(Locale.ROOT);
        return method.contains("trial") || method.contains("trail") || method.contains("limit");
    }

    private boolean isMqOtpPublish(ProceedingJoinPoint joinPoint) {
        return typeName(joinPoint).contains("Publisher") && "publish".equals(methodName(joinPoint));
    }

    private boolean isOtpVerifySpan(UaaObservation.OperationContext ctx) {
        return ctx != null && "uaa.otp.verify".equals(ctx.spanName());
    }

    private boolean isNib(ProceedingJoinPoint joinPoint) {
        return packageName(joinPoint).contains(".activation.nib");
    }

    private String methodName(ProceedingJoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
    }

    private String typeName(ProceedingJoinPoint joinPoint) {
        Class<?> type = joinPoint.getTarget() == null
                ? ((MethodSignature) joinPoint.getSignature()).getDeclaringType()
                : joinPoint.getTarget().getClass();
        return type.getSimpleName();
    }

    private String packageName(ProceedingJoinPoint joinPoint) {
        Class<?> type = joinPoint.getTarget() == null
                ? ((MethodSignature) joinPoint.getSignature()).getDeclaringType()
                : joinPoint.getTarget().getClass();
        Package currentPackage = type.getPackage();
        return currentPackage == null ? "" : currentPackage.getName();
    }

    private String normalize(String value) {
        String normalized = value == null ? "" : value
                .replaceAll("([a-z])([A-Z]+)", "$1.$2")
                .replaceAll("[^A-Za-z0-9]+", ".")
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.|\\.$", "")
                .toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? "unknown" : normalized;
    }
}
