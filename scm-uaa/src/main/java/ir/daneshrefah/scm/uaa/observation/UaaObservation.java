package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.LogObservationBuilder;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.TraceObservationBuilder;
import ir.daneshrefah.scm.observation.attributes.log.CommonLogAttributes;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaLogAttributes;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaTraceAttributes;
import org.springframework.stereotype.Component;

@Component
public class UaaObservation {
    private final ScmObservation observation;

    public UaaObservation(ScmObservation observation) {
        this.observation = observation;
    }

    public void authStarted(AuthContext ctx) {
        writeAuthLog("uaa.auth.started", "started", "UAA authentication started", "INFO", ctx, null);
    }

    public void authCompleted(AuthContext ctx) {
        writeAuthLog("uaa.auth.completed", "success", "UAA authentication completed", "INFO", ctx, null);
    }

    public void authFailed(AuthContext ctx, Throwable throwable) {
        writeAuthLog("uaa.auth.failed", "failure", "UAA authentication failed", "WARN", ctx, throwable);
    }

    public void jwtValidationStarted(JwtContext ctx) {
        writeJwtLog("uaa.jwt.validation.started", "started", "UAA JWT validation started", "INFO", ctx, null);
    }

    public void jwtValidationCompleted(JwtContext ctx) {
        writeJwtLog("uaa.jwt.validation.completed", "success", "UAA JWT validation completed", "INFO", ctx, null);
    }

    public void jwtValidationFailed(JwtContext ctx, Throwable throwable) {
        writeJwtLog("uaa.jwt.validation.failed", "failure", "UAA JWT validation failed", "WARN", ctx, throwable);
    }

    public void otpSent(OtpContext ctx) {
        writeOtpLog("uaa.otp.sent", "success", "UAA OTP sent", "INFO", ctx, null);
    }

    public void otpVerifyFailed(OtpContext ctx, Throwable throwable) {
        writeOtpLog("uaa.otp.verify.failed", "failure", "UAA OTP verification failed", "WARN", ctx, throwable);
    }

    public ObservationScope traceAuth(AuthContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.auth")
                .spanKind("internal")
                .action("uaa.auth");
        putAuthTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope traceJwtValidation(JwtContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.jwt.validation")
                .spanKind("internal")
                .action("uaa.jwt.validation");
        putJwtTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope traceOtpSend(OtpContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.otp.send")
                .spanKind("internal")
                .action("uaa.otp.send");
        putOtpTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope traceDbAuthentication(DbContext ctx) {
        return traceDb("uaa.db.authentication", ctx);
    }

    public ObservationScope traceDbActivation(DbContext ctx) {
        return traceDb("uaa.db.activation", ctx);
    }

    public ObservationScope traceMqOtpPublish(MessagingContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.mq.otp.publish")
                .spanKind("producer")
                .action("uaa.mq.otp.publish");
        putMessagingTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope authAttributes(ObservationScope scope, AuthContext ctx) {
        if (scope == null || ctx == null) {
            return scope;
        }
        scope.attribute(UaaTraceAttributes.AUTH_TYPE, ctx.authType())
                .attribute(UaaTraceAttributes.AUTH_CLIENT_ID, ctx.clientId())
                .attribute(UaaTraceAttributes.AUTH_GRANT_TYPE, ctx.grantType())
                .attribute(UaaTraceAttributes.AUTH_METHOD, ctx.authMethod())
                .attribute(UaaTraceAttributes.AUTH_STEP, ctx.step())
                .attribute(UaaTraceAttributes.AUTH_RESULT, ctx.result())
                .attribute(UaaTraceAttributes.AUTH_FAILURE_REASON, ctx.failureReason())
                .attribute(UaaTraceAttributes.JWT_PRESENT, ctx.jwtPresent())
                .attribute(UaaTraceAttributes.TOKEN_TYPE, ctx.tokenType())
                .attribute(UaaTraceAttributes.JWT_ISSUER, ctx.jwtIssuer())
                .attribute(UaaTraceAttributes.JWT_SUBJECT, ctx.jwtSubject())
                .attribute(UaaTraceAttributes.JWT_USERNAME, ctx.jwtUsername())
                .attribute(UaaTraceAttributes.JWT_MASKED, ctx.jwtMasked())
                .attribute(UaaTraceAttributes.JWT_EXPIRATION, ctx.jwtExpiration())
                .attribute(UaaTraceAttributes.CLIENT_IP, ctx.clientIp());
        return scope;
    }

    private ObservationScope traceDb(String spanName, DbContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span(spanName)
                .spanKind("client")
                .action(spanName);
        if (ctx != null) {
            builder.attribute(UaaTraceAttributes.DB_DATASOURCE, ctx.datasource())
                    .attribute(UaaTraceAttributes.AUTH_STEP, ctx.step());
        }
        return builder.start();
    }

    private void writeAuthLog(
            String action,
            String outcome,
            String message,
            String level,
            AuthContext ctx,
            Throwable throwable
    ) {
        LogObservationBuilder builder = baseLog(action, "uaa.auth", outcome, message, level);
        if (ctx != null) {
            builder.attribute(UaaLogAttributes.AUTH_TYPE, ctx.authType())
                    .attribute(UaaLogAttributes.AUTH_CLIENT_ID, ctx.clientId())
                    .attribute(UaaLogAttributes.AUTH_GRANT_TYPE, ctx.grantType())
                    .attribute(UaaLogAttributes.AUTH_METHOD, ctx.authMethod())
                    .attribute(UaaLogAttributes.AUTH_STEP, ctx.step())
                    .attribute(UaaLogAttributes.AUTH_RESULT, textOrDefault(ctx.result(), outcome))
                    .attribute(UaaLogAttributes.AUTH_FAILURE_REASON, textOrDefault(ctx.failureReason(), safeMessage(throwable)))
                    .attribute(UaaLogAttributes.JWT_PRESENT, ctx.jwtPresent())
                    .attribute(UaaLogAttributes.TOKEN_TYPE, ctx.tokenType())
                    .attribute(UaaLogAttributes.JWT_ISSUER, ctx.jwtIssuer())
                    .attribute(UaaLogAttributes.JWT_SUBJECT, ctx.jwtSubject())
                    .attribute(UaaLogAttributes.JWT_USERNAME, ctx.jwtUsername())
                    .attribute(UaaLogAttributes.JWT_MASKED, ctx.jwtMasked())
                    .attribute(UaaLogAttributes.JWT_EXPIRATION, ctx.jwtExpiration());
        }
        putError(builder, throwable);
        builder.write();
    }

    private void writeJwtLog(
            String action,
            String outcome,
            String message,
            String level,
            JwtContext ctx,
            Throwable throwable
    ) {
        LogObservationBuilder builder = baseLog(action, "uaa.jwt", outcome, message, level);
        if (ctx != null) {
            builder.attribute(UaaLogAttributes.JWT_PRESENT, ctx.present())
                    .attribute(UaaLogAttributes.TOKEN_TYPE, ctx.tokenType())
                    .attribute(UaaLogAttributes.JWT_ISSUER, ctx.issuer())
                    .attribute(UaaLogAttributes.JWT_SUBJECT, ctx.subject())
                    .attribute(UaaLogAttributes.JWT_USERNAME, ctx.username())
                    .attribute(UaaLogAttributes.JWT_MASKED, ctx.jwtMasked())
                    .attribute(UaaLogAttributes.JWT_EXPIRATION, ctx.expiration());
        }
        putError(builder, throwable);
        builder.write();
    }

    private void writeOtpLog(
            String action,
            String outcome,
            String message,
            String level,
            OtpContext ctx,
            Throwable throwable
    ) {
        LogObservationBuilder builder = baseLog(action, "uaa.otp", outcome, message, level);
        if (ctx != null) {
            builder.attribute(UaaLogAttributes.OTP_CHANNEL, ctx.channel())
                    .attribute(UaaLogAttributes.OTP_PURPOSE, ctx.purpose())
                    .attribute(UaaLogAttributes.AUTH_STEP, ctx.step())
                    .attribute(UaaLogAttributes.AUTH_RESULT, textOrDefault(ctx.result(), outcome))
                    .attribute(UaaLogAttributes.AUTH_FAILURE_REASON, textOrDefault(ctx.failureReason(), safeMessage(throwable)));
        }
        putError(builder, throwable);
        builder.write();
    }

    private LogObservationBuilder baseLog(String action, String category, String outcome, String message, String level) {
        return observation.log()
                .event()
                .source(UaaObservation.class)
                .loggerName(UaaObservation.class)
                .category(category)
                .action(action)
                .outcome(outcome)
                .level(level)
                .message(message);
    }

    private void putAuthTraceAttributes(TraceObservationBuilder builder, AuthContext ctx) {
        if (ctx == null) {
            return;
        }
        builder.attribute(UaaTraceAttributes.AUTH_TYPE, ctx.authType())
                .attribute(UaaTraceAttributes.AUTH_CLIENT_ID, ctx.clientId())
                .attribute(UaaTraceAttributes.AUTH_GRANT_TYPE, ctx.grantType())
                .attribute(UaaTraceAttributes.AUTH_METHOD, ctx.authMethod())
                .attribute(UaaTraceAttributes.AUTH_STEP, ctx.step())
                .attribute(UaaTraceAttributes.AUTH_RESULT, ctx.result())
                .attribute(UaaTraceAttributes.AUTH_FAILURE_REASON, ctx.failureReason())
                .attribute(UaaTraceAttributes.JWT_PRESENT, ctx.jwtPresent())
                .attribute(UaaTraceAttributes.TOKEN_TYPE, ctx.tokenType())
                .attribute(UaaTraceAttributes.JWT_ISSUER, ctx.jwtIssuer())
                .attribute(UaaTraceAttributes.JWT_SUBJECT, ctx.jwtSubject())
                .attribute(UaaTraceAttributes.JWT_USERNAME, ctx.jwtUsername())
                .attribute(UaaTraceAttributes.JWT_MASKED, ctx.jwtMasked())
                .attribute(UaaTraceAttributes.JWT_EXPIRATION, ctx.jwtExpiration())
                .attribute(UaaTraceAttributes.CLIENT_IP, ctx.clientIp());
    }

    private void putJwtTraceAttributes(TraceObservationBuilder builder, JwtContext ctx) {
        if (ctx == null) {
            return;
        }
        builder.attribute(UaaTraceAttributes.JWT_PRESENT, ctx.present())
                .attribute(UaaTraceAttributes.TOKEN_TYPE, ctx.tokenType())
                .attribute(UaaTraceAttributes.JWT_ISSUER, ctx.issuer())
                .attribute(UaaTraceAttributes.JWT_SUBJECT, ctx.subject())
                .attribute(UaaTraceAttributes.JWT_USERNAME, ctx.username())
                .attribute(UaaTraceAttributes.JWT_MASKED, ctx.jwtMasked())
                .attribute(UaaTraceAttributes.JWT_EXPIRATION, ctx.expiration());
    }

    private void putOtpTraceAttributes(TraceObservationBuilder builder, OtpContext ctx) {
        if (ctx == null) {
            return;
        }
        builder.attribute(UaaTraceAttributes.OTP_CHANNEL, ctx.channel())
                .attribute(UaaTraceAttributes.OTP_PURPOSE, ctx.purpose())
                .attribute(UaaTraceAttributes.AUTH_STEP, ctx.step())
                .attribute(UaaTraceAttributes.AUTH_RESULT, ctx.result())
                .attribute(UaaTraceAttributes.AUTH_FAILURE_REASON, ctx.failureReason());
    }

    private void putMessagingTraceAttributes(TraceObservationBuilder builder, MessagingContext ctx) {
        if (ctx == null) {
            return;
        }
        builder.attribute(UaaTraceAttributes.MESSAGING_SYSTEM, ctx.system())
                .attribute(UaaTraceAttributes.MESSAGING_DESTINATION_NAME, ctx.destinationName())
                .attribute(UaaTraceAttributes.OTP_CHANNEL, ctx.otpChannel())
                .attribute(UaaTraceAttributes.OTP_PURPOSE, ctx.otpPurpose());
    }

    private void putError(LogObservationBuilder builder, Throwable throwable) {
        if (throwable == null) {
            return;
        }
        builder.attribute(CommonLogAttributes.ERROR_TYPE, throwable.getClass().getName())
                .attribute(CommonLogAttributes.ERROR_MESSAGE, safeMessage(throwable));
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return null;
        }
        String message = throwable.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|card[_-]?number)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    public record AuthContext(
            String authType,
            String clientId,
            String grantType,
            String authMethod,
            String step,
            String result,
            String failureReason,
            String username,
            String clientIp,
            Boolean jwtPresent,
            String tokenType,
            String jwtIssuer,
            String jwtSubject,
            String jwtUsername,
            String jwtMasked,
            String jwtExpiration
    ) {
        public AuthContext withResult(String result) {
            return new AuthContext(authType, clientId, grantType, authMethod, step, result, failureReason, username,
                    clientIp, jwtPresent, tokenType, jwtIssuer, jwtSubject, jwtUsername, jwtMasked, jwtExpiration);
        }

        public AuthContext withFailureReason(String failureReason) {
            return new AuthContext(authType, clientId, grantType, authMethod, step, result, failureReason, username,
                    clientIp, jwtPresent, tokenType, jwtIssuer, jwtSubject, jwtUsername, jwtMasked, jwtExpiration);
        }

        public AuthContext withJwtPresent(Boolean jwtPresent) {
            return new AuthContext(authType, clientId, grantType, authMethod, step, result, failureReason, username,
                    clientIp, jwtPresent, tokenType, jwtIssuer, jwtSubject, jwtUsername, jwtMasked, jwtExpiration);
        }

        public AuthContext withJwtUsername(String jwtUsername) {
            return new AuthContext(authType, clientId, grantType, authMethod, step, result, failureReason, username,
                    clientIp, jwtPresent, tokenType, jwtIssuer, jwtSubject, jwtUsername, jwtMasked, jwtExpiration);
        }
    }

    public record JwtContext(
            Boolean present,
            String tokenType,
            String issuer,
            String subject,
            String username,
            String jwtMasked,
            String expiration
    ) {
    }

    public record OtpContext(
            String channel,
            String purpose,
            String step,
            String result,
            String failureReason
    ) {
    }

    public record DbContext(String datasource, String step) {
    }

    public record MessagingContext(
            String system,
            String destinationName,
            String otpChannel,
            String otpPurpose
    ) {
    }
}
