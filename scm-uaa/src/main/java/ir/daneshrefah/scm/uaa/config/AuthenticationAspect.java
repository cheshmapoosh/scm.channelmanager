package ir.daneshrefah.scm.uaa.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.utils.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


//INSERT INTO LOGGER.MESSAGE_LOG (ID, CORRELATION_ID, USERNAME, ACCESS_PARAM, BODY, IP, STATUS, TRANSACTION_DATE, SERVICE_TYPE, REAL_USERNAME, SUBMIT_DATE, ALLOCATED_TIME, CLIENT_TYPE, AMOUNT) VALUES (18188621, '1640862529925594', '5532377608', null, '{"description":"Success","serverCode":"1","additionalStatus":null,"name":"SUCCESS","code":"0","severity":"INFO"}', null, 'RESPONSE_FROM_CHANNEL', '2021-12-30 11:07:11.468000', 'LoanListInquiryResponse', '5532377608', '2021-12-30 14:37:20.150265', 0, null, null);
@Aspect
@Component
@ConditionalOnProperty(name = "scm.log.trace.aspect.enable", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class AuthenticationAspect {

//    private static final Logger traceLogger = LoggerFactory.getLogger("TRACE");
    private final Tracer tracer;
//    private final Logger logger;
    private ObjectMapper objectMapper;
    private final JwtDecoder jwtDecoder;

    @Around("execution(* authenticate(..)) && args(authentication) && within(ir.daneshrefah.scm.uaa.security.authenticationProvider.OAuth2GeneralAuthenticationProvider)")
    public Object traceAuthenticate(ProceedingJoinPoint joinPoint, Authentication authentication) throws Throwable {
        Span span = tracer.spanBuilder("AuthenticationProvider.authenticate").setSpanKind(SpanKind.INTERNAL).startSpan();


        MDC.put("traceId", span.getSpanContext().getTraceId());

        boolean hasError = false;
        try (Scope scope = span.makeCurrent()) {

//            SpanUtil.setSpanAttributes(span);
            span.setAttribute("spanId", span.getSpanContext().getSpanId());
            if (authentication instanceof PreAuthenticationToken preAuthenticationToken) {
                objectMapper = new ObjectMapper();
                span.setAttribute("nickName", preAuthenticationToken.getUsername());
                span.setAttribute("accessParam", preAuthenticationToken.getAccessParameter());
                span.setAttribute("requestBody", preAuthenticationToken.getDefaultGrantPreAuthToken() != null ? objectMapper.writeValueAsString(preAuthenticationToken.getDefaultGrantPreAuthToken()) : "");
                span.setAttribute("ip", preAuthenticationToken.getRemoteAddress());
                span.setAttribute("status", "REQUEST_TO_CHANNEL");
                span.setAttribute("transactionDate", DateUtils.LocalDateTimeTools.getCurrentLocalDateTime().toString());
                span.setAttribute("serviceType", "loginRequest");
                span.setAttribute("submitDate", DateUtils.LocalDateTimeTools.getCurrentLocalDateTime().toString());
                span.setAttribute("scm-source",Boolean.TRUE);
                span.setAttribute("version", "8.5.2");
                span.setAttribute("correlationId", span.getSpanContext().getTraceId());
                String clientType;
                if (preAuthenticationToken.hasDefaultGrantPreAuthToken()) {
                    clientType = preAuthenticationToken.getDefaultGrantPreAuthToken().getAppVersion();
                } else {
                    clientType = preAuthenticationToken.getClientId();
                }
                span.setAttribute("clientType", clientType);
            }

            long startTime = System.currentTimeMillis();

            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            long durationMillis = endTime - startTime;
            span.setAttribute("allocatedTime", durationMillis);
            if (result instanceof OAuth2AccessTokenAuthenticationToken oAuth2AccessTokenAuthenticationToken) {
                String tokenValue = oAuth2AccessTokenAuthenticationToken.getAccessToken().getTokenValue();
                Jwt decode = jwtDecoder.decode(tokenValue);
                Map<String,String> responseMap=new HashMap<>();
                responseMap.put("jwt", tokenValue);
                responseMap.put("lastName",decode.getClaim(Constants.CLAIM_KEY_PERSON_LAST_NAME));
                responseMap.put("firstName",decode.getClaim(Constants.CLAIM_KEY_PERSON_FIRST_NAME));
                String ppi = decode.getClaim(Constants.CLAIM_KEY_PERSON_PROFILE_IDENTIFIER);
                responseMap.put("username",decode.getClaim(Constants.CLAIM_KEY_PERSON_FIRST_NAME));
                span.setAttribute("username", ppi);
                span.setAttribute("realUsername", ppi);

                span.setAttribute("responseBody", new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(responseMap));

            }

            span.setAttribute("responseStatus", "RESPONSE_FROM_CHANNEL");

            span.setStatus(StatusCode.OK);
            return result;
        } catch (AuthenticationException ex) {
            hasError = true;
            span.recordException(ex);
            span.setAttribute("responseStatus", "RESPONSE_FAILED");
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            log.trace("Authentication failed", ex);
            throw ex;
        } catch (Throwable ex) {
            hasError = true;
            span.setAttribute("responseStatus", "RESPONSE_FAILED");
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, "Unexpected error");
            log.error("Unexpected error during authentication", ex);
            throw ex;
        } finally {
            if (!hasError) {
                span.setStatus(StatusCode.OK);
            }
            span.end();
            MDC.remove("traceId");
        }
    }
}
