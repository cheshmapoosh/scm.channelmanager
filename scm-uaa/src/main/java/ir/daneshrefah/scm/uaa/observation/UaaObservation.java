package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.CorrelationType;
import ir.daneshrefah.scm.observation.LogObservationBuilder;
import ir.daneshrefah.scm.observation.ObservationIds;
import ir.daneshrefah.scm.observation.ObservationScope;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.TraceContext;
import ir.daneshrefah.scm.observation.TraceContextHolder;
import ir.daneshrefah.scm.observation.TraceObservationBuilder;
import ir.daneshrefah.scm.observation.attributes.log.CommonLogAttributes;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaLogAttributes;
import ir.daneshrefah.scm.uaa.observation.attributes.UaaTraceAttributes;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

@Component
public class UaaObservation {
    private static final String REQUEST = CorrelationType.REQUEST.value();
    private static final int MAX_SAFE_MESSAGE_LENGTH = 300;
    private static final String LEGACY_LOGIN = "login";
    private static final Set<String> MISSING_CHANNEL_CODES = Set.of(
            "null",
            "blank",
            "unknown",
            "default",
            "none",
            "n/a",
            "n-a"
    );

    private final ScmObservation observation;
    private final Environment environment;

    public UaaObservation(ScmObservation observation, Environment environment) {
        this.observation = observation;
        this.environment = environment;
    }

    public void controllerStarted(ControllerContext ctx) {
        writeControllerLog(
                ctx,
                "started",
                "INFO",
                "UAA controller request started",
                null
        );
    }

    public void controllerCompleted(ControllerContext ctx) {
        writeControllerLog(
                ctx == null ? null : ctx.withResult("success"),
                "success",
                "INFO",
                "UAA controller request completed",
                null
        );
    }

    public void controllerFailed(ControllerContext ctx, Throwable throwable) {
        writeControllerLog(
                ctx == null ? null : ctx.withResult("failure").withFailureReason(safeMessage(throwable)),
                "failure",
                "WARN",
                "UAA controller request failed",
                throwable
        );
    }

    public ObservationScope traceController(ControllerContext ctx) {
        String spanName = ctx == null ? "uaa.controller.unknown.unknown" : ctx.spanName();
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span(spanName)
                .spanKind("server")
                .action(spanName)
                .correlationId(ctx == null ? null : ctx.correlationId())
                .correlationType(REQUEST)
                .traceId(ObservationIds.traceId())
                .parentSpanId("");
        putControllerTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope controllerAttributes(ObservationScope scope, ControllerContext ctx) {
        if (scope == null || ctx == null) {
            return scope;
        }
        scope.attribute(UaaTraceAttributes.HTTP_METHOD, ctx.httpMethod())
                .attribute(UaaTraceAttributes.URL_PATH, ctx.path())
                .attribute(UaaTraceAttributes.HTTP_STATUS_CODE, ctx.statusCode())
                .attribute(UaaTraceAttributes.CLIENT_IP, ctx.clientIp())
                .attribute(UaaTraceAttributes.AUTH_STEP, ctx.operation())
                .attribute(UaaTraceAttributes.AUTH_RESULT, ctx.result())
                .attribute(UaaTraceAttributes.AUTH_FAILURE_REASON, ctx.failureReason());
        return scope;
    }

    public void operationStarted(OperationContext ctx) {
        writeOperationLog(ctx, "started", "INFO", "UAA operation started", null);
    }

    public void operationCompleted(OperationContext ctx) {
        writeOperationLog(ctx == null ? null : ctx.withResult("success"), "success", "INFO",
                "UAA operation completed", null);
    }

    public void operationFailed(OperationContext ctx, Throwable throwable) {
        writeOperationLog(ctx == null ? null : ctx.withResult("failure").withFailureReason(safeMessage(throwable)),
                "failure", "WARN", "UAA operation failed", throwable);
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

    public void jwtIssueStarted(JwtContext ctx) {
        writeJwtLog("uaa.jwt.issue.started", "started", "UAA JWT issue started", "INFO", ctx, null);
    }

    public void jwtIssueCompleted(JwtContext ctx) {
        writeJwtLog("uaa.jwt.issue.completed", "success", "UAA JWT issue completed", "INFO", ctx, null);
    }

    public void jwtIssueFailed(JwtContext ctx, Throwable throwable) {
        writeJwtLog("uaa.jwt.issue.failed", "failure", "UAA JWT issue failed", "WARN", ctx, throwable);
    }

    public void otpSent(OtpContext ctx) {
        writeOtpLog("uaa.otp.sent", "success", "UAA OTP sent", "INFO", ctx, null);
    }

    public void otpVerifyStarted(OtpContext ctx) {
        writeOtpLog("uaa.otp.verify.started", "started", "UAA OTP verification started", "INFO", ctx, null);
    }

    public void otpVerifyCompleted(OtpContext ctx) {
        writeOtpLog("uaa.otp.verify.completed", "success", "UAA OTP verification completed", "INFO", ctx, null);
    }

    public void otpVerifyFailed(OtpContext ctx, Throwable throwable) {
        writeOtpLog("uaa.otp.verify.failed", "failure", "UAA OTP verification failed", "WARN", ctx, throwable);
    }

    public ObservationScope traceAuth(AuthContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.auth.login")
                .spanKind("internal")
                .action("uaa.auth.login");
        if (TraceContextHolder.current() == null) {
            builder.correlationType(REQUEST);
        }
        putAuthTraceAttributes(builder, ctx);
        putLegacyProjectionAttributes(builder, LEGACY_LOGIN);
        String channelCode = channelCodeForClient(ctx == null ? null : ctx.clientId());
        if (channelCode != null) {
            builder.attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, channelCode);
        }
        return builder.start();
    }

    public ObservationScope traceLegacyBusinessOperation(String operationCode, String spanName, String channelCode) {
        String operation = textOrDefault(operationCode, "unknown");
        String span = textOrDefault(spanName, "uaa.business." + normalizeName(operation));
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span(span)
                .spanKind("server")
                .action(span)
                .correlationType(REQUEST)
                .traceId(ObservationIds.traceId())
                .parentSpanId("");
        putLegacyProjectionAttributes(builder, operation);
        builder.attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, resolveBusinessChannelCode(channelCode));
        builder.attribute(UaaTraceAttributes.AUTH_STEP, operation);
        return builder.start();
    }

    public ObservationScope traceSecurity(OperationContext ctx) {
        return traceOperation(ctx, "uaa.auth.provider.authenticate", "uaa.auth", "internal");
    }

    public ObservationScope traceJwtIssue(JwtContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.jwt.issue")
                .spanKind("internal")
                .action("uaa.jwt.issue");
        putJwtTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope traceJwtValidation(JwtContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.jwt.validate")
                .spanKind("internal")
                .action("uaa.jwt.validate");
        putJwtTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope traceActivationPwa(OperationContext ctx) {
        return traceOperation(ctx, "uaa.activation.pwa.request", "uaa.activation.pwa", "internal");
    }

    public ObservationScope traceActivationNib(OperationContext ctx) {
        return traceOperation(ctx, "uaa.activation.nib.request", "uaa.activation.nib", "internal");
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

    public ObservationScope traceOtpVerify(OtpContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.otp.verify")
                .spanKind("internal")
                .action("uaa.otp.verify");
        putOtpTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope traceOtpBlockCheck(OtpContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.otp.block.check")
                .spanKind("internal")
                .action("uaa.otp.block.check");
        putOtpTraceAttributes(builder, ctx);
        return builder.start();
    }

    public ObservationScope traceOtpTrialUpdate(OtpContext ctx) {
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span("uaa.otp.trial.update")
                .spanKind("internal")
                .action("uaa.otp.trial.update");
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
        putUaaMessagingAttributes(builder, ctx);
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

    public ObservationScope operationAttributes(ObservationScope scope, OperationContext ctx) {
        if (scope == null || ctx == null) {
            return scope;
        }
        scope.attribute(UaaTraceAttributes.AUTH_STEP, ctx.step())
                .attribute(UaaTraceAttributes.AUTH_RESULT, ctx.result())
                .attribute(UaaTraceAttributes.AUTH_FAILURE_REASON, ctx.failureReason());
        return scope;
    }

    public ObservationScope jwtAttributes(ObservationScope scope, JwtContext ctx) {
        if (scope == null || ctx == null) {
            return scope;
        }
        scope.attribute(UaaTraceAttributes.JWT_PRESENT, ctx.present())
                .attribute(UaaTraceAttributes.TOKEN_TYPE, ctx.tokenType())
                .attribute(UaaTraceAttributes.JWT_ISSUER, ctx.issuer())
                .attribute(UaaTraceAttributes.JWT_SUBJECT, ctx.subject())
                .attribute(UaaTraceAttributes.JWT_USERNAME, ctx.username())
                .attribute(UaaTraceAttributes.JWT_MASKED, ctx.jwtMasked())
                .attribute(UaaTraceAttributes.JWT_EXPIRATION, ctx.expiration());
        return scope;
    }

    public String jwtMasked(String token) {
        String value = textOrNull(token);
        if (value == null) {
            return null;
        }
        if (value.length() <= 10) {
            return value;
        }
        return value.substring(0, 5) + "..." + value.substring(value.length() - 5);
    }

    public String safeErrorMessage(Throwable throwable) {
        return safeMessage(throwable);
    }

    public String resolveBusinessChannelCode(String value) {
        String mappedClientChannel = channelCodeForClient(value);
        if (mappedClientChannel != null) {
            return mappedClientChannel;
        }
        String normalized = normalizeChannelCode(value);
        return "ib".equals(normalized) || "mb".equals(normalized) ? normalized : null;
    }

    public String maskPhone(String value) {
        String phone = textOrNull(value);
        if (phone == null) {
            return null;
        }
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() <= 4) {
            return "****";
        }
        return "***" + digits.substring(digits.length() - 4);
    }

    private ObservationScope traceOperation(
            OperationContext ctx,
            String defaultSpanName,
            String defaultCategory,
            String spanKind
    ) {
        String spanName = textOrDefault(ctx == null ? null : ctx.spanName(), defaultSpanName);
        TraceObservationBuilder builder = observation.trace()
                .source(UaaObservation.class)
                .span(spanName)
                .spanKind(textOrDefault(spanKind, "internal"))
                .action(spanName);
        putOperationTraceAttributes(builder, ctx, defaultCategory);
        return builder.start();
    }

    private ObservationScope traceDb(String baseSpanName, DbContext ctx) {
        String spanName = dbSpanName(baseSpanName, ctx);
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

    private void writeControllerLog(
            ControllerContext ctx,
            String outcome,
            String level,
            String message,
            Throwable throwable
    ) {
        String action = ctx == null ? "uaa.controller.unknown.unknown" : ctx.spanName();
        LogObservationBuilder builder = baseLog(action + "." + outcome, "uaa.controller", outcome, message, level)
                .correlationId(ctx == null ? null : ctx.correlationId())
                .correlationType(REQUEST);
        if (ctx != null) {
            builder.attribute(UaaLogAttributes.AUTH_STEP, ctx.operation())
                    .attribute(UaaLogAttributes.AUTH_RESULT, textOrDefault(ctx.result(), outcome))
                    .attribute(UaaLogAttributes.AUTH_FAILURE_REASON, textOrDefault(ctx.failureReason(), safeMessage(throwable)))
                    .attribute(UaaLogAttributes.HTTP_METHOD, ctx.httpMethod())
                    .attribute(UaaLogAttributes.URL_PATH, ctx.path())
                    .attribute(UaaLogAttributes.HTTP_STATUS_CODE, ctx.statusCode())
                    .attribute(UaaLogAttributes.CLIENT_IP, ctx.clientIp());
        }
        putError(builder, throwable);
        builder.write();
    }

    private void writeOperationLog(
            OperationContext ctx,
            String outcome,
            String level,
            String message,
            Throwable throwable
    ) {
        String category = textOrDefault(ctx == null ? null : ctx.category(), "uaa.operation");
        String action = textOrDefault(ctx == null ? null : ctx.spanName(), category);
        LogObservationBuilder builder = baseLog(action + "." + outcome, category, outcome, message, level);
        if (ctx != null) {
            builder.attribute(UaaLogAttributes.AUTH_STEP, ctx.step())
                    .attribute(UaaLogAttributes.AUTH_RESULT, textOrDefault(ctx.result(), outcome))
                    .attribute(UaaLogAttributes.AUTH_FAILURE_REASON, textOrDefault(ctx.failureReason(), safeMessage(throwable)));
        }
        putError(builder, throwable);
        builder.write();
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
                    .attribute(UaaLogAttributes.JWT_EXPIRATION, ctx.jwtExpiration())
                    .attribute(UaaLogAttributes.CLIENT_IP, ctx.clientIp());
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
        LogObservationBuilder builder = observation.log()
                .event()
                .source(UaaObservation.class)
                .loggerName(UaaObservation.class)
                .category(category)
                .action(action)
                .outcome(outcome)
                .level(level)
                .message(message);
        TraceContext current = TraceContextHolder.current();
        if (current != null) {
            builder.correlationId(current.correlationId())
                    .correlationType(current.correlationType())
                    .attribute(CommonLogAttributes.TRACE_ID, current.traceId())
                    .attribute(CommonLogAttributes.SPAN_ID, current.spanId());
        }
        return builder;
    }

    private void putControllerTraceAttributes(TraceObservationBuilder builder, ControllerContext ctx) {
        if (ctx == null) {
            return;
        }
        builder.attribute(UaaTraceAttributes.HTTP_METHOD, ctx.httpMethod())
                .attribute(UaaTraceAttributes.URL_PATH, ctx.path())
                .attribute(UaaTraceAttributes.HTTP_STATUS_CODE, ctx.statusCode())
                .attribute(UaaTraceAttributes.CLIENT_IP, ctx.clientIp())
                .attribute(UaaTraceAttributes.AUTH_STEP, ctx.operation())
                .attribute(UaaTraceAttributes.AUTH_RESULT, ctx.result())
                .attribute(UaaTraceAttributes.AUTH_FAILURE_REASON, ctx.failureReason());
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

    private void putLegacyProjectionAttributes(TraceObservationBuilder builder, String operationCode) {
        String operation = textOrNull(operationCode);
        if (operation == null) {
            return;
        }
        builder.attribute("scm.observation.legacy.enabled", true)
                .attribute("scm.observation.legacy.operation.code", legacyOperationCode(operation))
                .attribute("scm.observation.legacy.service.code", legacyServiceCode(operation));
    }

    private String legacyOperationCode(String operationCode) {
        return firstText(
                environmentValue("scm.uaa.observation.legacy.operations." + operationCode + ".operation-code"),
                operationCode
        );
    }

    private String legacyServiceCode(String operationCode) {
        return environmentValue("scm.uaa.observation.legacy.operations." + operationCode + ".service-code");
    }

    private String environmentValue(String key) {
        return environment == null ? null : environment.getProperty(key);
    }

    private String channelCodeForClient(String clientId) {
        String client = normalizeLookup(clientId);
        if (client == null) {
            return null;
        }
        String configured = configuredClientChannel(client);
        if (configured != null) {
            return configured;
        }
        if (matchesClient(client, environmentValue("scm.uaa.legacy.client-resolution.nib-client-id"))
                || "nib".equals(client)
                || "ib".equals(client)
                || "internet".equals(client)
                || "internet-banking".equals(client)) {
            return "ib";
        }
        if (matchesClient(client, environmentValue("scm.uaa.legacy.client-resolution.pwa-client-id"))
                || matchesClient(client, environmentValue("scm.uaa.legacy.client-resolution.mb-client-id"))
                || matchesClient(client, environmentValue("scm.uaa.legacy.client-resolution.super-app-client-id"))
                || "pwa".equals(client)
                || "mb".equals(client)
                || "mobile".equals(client)
                || "sa".equals(client)
                || "super-app".equals(client)) {
            return "mb";
        }
        return null;
    }

    private String configuredClientChannel(String normalizedClientId) {
        String mappings = environmentValue("scm.uaa.observation.channel.client-mappings");
        if (mappings == null || mappings.isBlank()) {
            return null;
        }
        return Arrays.stream(mappings.split("[,;]"))
                .map(String::trim)
                .filter(mapping -> !mapping.isBlank())
                .map(mapping -> mapping.split("=", 2))
                .filter(parts -> parts.length == 2)
                .filter(parts -> normalizedClientId.equals(normalizeLookup(parts[0])))
                .map(parts -> normalizeChannelCode(parts[1]))
                .filter(channel -> channel != null)
                .findFirst()
                .orElse(null);
    }

    private boolean matchesClient(String normalizedClientId, String configuredClientId) {
        String configured = normalizeLookup(configuredClientId);
        return configured != null && configured.equals(normalizedClientId);
    }

    private String normalizeChannelCode(String value) {
        String channel = normalizeLookup(value);
        if (channel == null || MISSING_CHANNEL_CODES.contains(channel)) {
            return null;
        }
        return channel;
    }

    private String normalizeLookup(String value) {
        String text = textOrNull(value);
        return text == null ? null : text.toLowerCase(Locale.ROOT);
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

    private void putOperationTraceAttributes(
            TraceObservationBuilder builder,
            OperationContext ctx,
            String defaultCategory
    ) {
        if (ctx == null) {
            builder.attribute(UaaTraceAttributes.AUTH_TYPE, defaultCategory);
            return;
        }
        builder.attribute(UaaTraceAttributes.AUTH_TYPE, textOrDefault(ctx.category(), defaultCategory))
                .attribute(UaaTraceAttributes.AUTH_STEP, ctx.step())
                .attribute(UaaTraceAttributes.AUTH_RESULT, ctx.result())
                .attribute(UaaTraceAttributes.AUTH_FAILURE_REASON, ctx.failureReason());
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

    private void putUaaMessagingAttributes(TraceObservationBuilder builder, MessagingContext ctx) {
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

    private String dbSpanName(String baseSpanName, DbContext ctx) {
        String step = ctx == null ? null : textOrNull(ctx.step());
        if (step == null) {
            return baseSpanName;
        }
        String normalizedStep = normalizeName(step);
        if (normalizedStep.startsWith(baseSpanName + ".")) {
            return normalizedStep;
        }
        return baseSpanName + "." + normalizedStep;
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
        return message.length() > MAX_SAFE_MESSAGE_LENGTH ? message.substring(0, MAX_SAFE_MESSAGE_LENGTH) : message;
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstText(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return null;
    }

    private String normalizeName(String value) {
        String normalized = textOrDefault(value, "unknown")
                .replaceAll("([a-z])([A-Z]+)", "$1.$2")
                .replaceAll("[^A-Za-z0-9]+", ".")
                .replaceAll("\\.+", ".")
                .replaceAll("^\\.|\\.$", "")
                .toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? "unknown" : normalized;
    }

    public record ControllerContext(
            String controller,
            String operation,
            String httpMethod,
            String path,
            Integer statusCode,
            String clientIp,
            String correlationId,
            String result,
            String failureReason
    ) {
        public String spanName() {
            return "uaa.controller." + normalize(controller) + "." + normalize(operation);
        }

        public ControllerContext withStatus(Integer statusCode) {
            return new ControllerContext(controller, operation, httpMethod, path, statusCode, clientIp, correlationId,
                    result, failureReason);
        }

        public ControllerContext withResult(String result) {
            return new ControllerContext(controller, operation, httpMethod, path, statusCode, clientIp, correlationId,
                    result, failureReason);
        }

        public ControllerContext withFailureReason(String failureReason) {
            return new ControllerContext(controller, operation, httpMethod, path, statusCode, clientIp, correlationId,
                    result, failureReason);
        }

        private static String normalize(String value) {
            if (value == null || value.isBlank()) {
                return "unknown";
            }
            String normalized = value
                    .replaceAll("([a-z])([A-Z]+)", "$1.$2")
                    .replaceAll("[^A-Za-z0-9]+", ".")
                    .replaceAll("\\.+", ".")
                    .replaceAll("^\\.|\\.$", "")
                    .toLowerCase(Locale.ROOT);
            return normalized.isBlank() ? "unknown" : normalized;
        }
    }

    public record OperationContext(
            String spanName,
            String category,
            String step,
            String result,
            String failureReason
    ) {
        public OperationContext withResult(String result) {
            return new OperationContext(spanName, category, step, result, failureReason);
        }

        public OperationContext withFailureReason(String failureReason) {
            return new OperationContext(spanName, category, step, result, failureReason);
        }
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
        public JwtContext withMasked(String jwtMasked) {
            return new JwtContext(present, tokenType, issuer, subject, username, jwtMasked, expiration);
        }
    }

    public record OtpContext(
            String channel,
            String purpose,
            String step,
            String result,
            String failureReason
    ) {
        public OtpContext withResult(String result) {
            return new OtpContext(channel, purpose, step, result, failureReason);
        }

        public OtpContext withFailureReason(String failureReason) {
            return new OtpContext(channel, purpose, step, result, failureReason);
        }
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
