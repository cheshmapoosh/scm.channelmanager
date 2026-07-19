package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.exception.CardException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Locale;
import java.util.Optional;

import static ir.daneshrefah.scm.common.constant.BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_KEY;
import static ir.daneshrefah.scm.common.constant.BundleDefaults.EXCEPTION_BUNDLE_DEFAULT_PREFIX;

@Component("legacyMbFaultEncoder")
@RequiredArgsConstructor
@Slf4j
public class LegacyMbFaultEncoder implements FaultContractEncoder {

    private static final int LEGACY_SECURITY_HTTP_STATUS = HttpStatus.NOT_ACCEPTABLE.value();

    private final ObjectMapper objectMapper;
    private final ResourceBundleService resourceBundleService;

    @Override
    public Object encode(
            Exchange exchange,
            ScmFault fault,
            ClientContract contract) {

        log.info(
                "Encoding legacy MB fault: exchangeId={}, contract={}, errorCount={}",
                exchange.getExchangeId(),
                contract,
                fault != null && fault.getErrors() != null
                        ? fault.getErrors().size()
                        : 0);

        try {
            LegacyMbFaultResponse response = encodeFault(exchange, fault);

            log.info(
                    "Legacy MB fault encoded: exchangeId={}, httpStatus={}, code={}, messageKey={}",
                    exchange.getExchangeId(),
                    response.httpCode(),
                    response.code(),
                    response.messageKey());

            return response;
        } catch (Exception exception) {
            log.error(
                    "Could not encode legacy MB fault: exchangeId={}",
                    exchange.getExchangeId(),
                    exception);

            exchange.getMessage().setHeader(
                    Exchange.HTTP_RESPONSE_CODE,
                    500);

            return new LegacyMbFaultResponse(
                    "خطای داخلی رخ داده است",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    MessageStatus.SC_ERROR_SYSTEM.name(),
                    "internal server error",
                    HttpStatus.INTERNAL_SERVER_ERROR.name());
        }
    }

    private LegacyMbFaultResponse encodeFault(
            Exchange exchange,
            ScmFault fault) {

        if (fault == null ||
                fault.getErrors() == null ||
                fault.getErrors().isEmpty()) {

            log.error(
                    "Cannot encode legacy MB fault because no error is available: exchangeId={}",
                    exchange.getExchangeId());

            throw new IllegalArgumentException(
                    "ScmFault must contain at least one error");
        }

        Error error = fault.getErrors().getFirst();

        log.info(
                "Processing SCM error: exchangeId={}, errorCode={}, status={}, exceptionClass={}",
                exchange.getExchangeId(),
                error.getErrorCode(),
                error.getStatus(),
                error.getException() != null
                        ? error.getException().getClass().getName()
                        : null);

        Integer httpStatus = Optional
                .ofNullable(HttpStatusMapper.toHttpStatus(error.getStatus()))
                .orElse(500);

        Locale requestLocale = resolveRequestLocale(exchange);
        String bundleKey = resolveBundleKey(error);

        String message = resolveBundleMessage(
                requestLocale,
                bundleKey,
                error.getMessage());

        String detail = resolveBundleMessage(
                AccessibleLocale.FA_IR.getLocale(),
                bundleKey,
                error.getMessageFa());

        MessageStatus responseStatus = error.getStatus() != null
                ? error.getStatus()
                : MessageStatus.SC_ERROR_SYSTEM;

        if (responseStatus == MessageStatus.INCORRECT_PIN) {
            responseStatus = MessageStatus.SECURITY_VIOLATION;
            httpStatus = LEGACY_SECURITY_HTTP_STATUS;
        }

        log.info(
                "Legacy fault values resolved: exchangeId={}, errorCode={}, responseCode={}, httpStatus={}, locale={}, bundleKey={}",
                exchange.getExchangeId(),
                error.getErrorCode(),
                httpStatus,
                httpStatus,
                requestLocale,
                bundleKey);

        if (error.getException() instanceof HttpClientErrorException exception) {
            String responseBody = exception.getResponseBodyAsString();

            log.info(
                    "HttpClientErrorException detected: exchangeId={}, providerStatus={}, responseBodyLength={}",
                    exchange.getExchangeId(),
                    exception.getStatusCode().value(),
                    responseBody != null ? responseBody.length() : 0);

            try {
                JsonNode root = objectMapper.readTree(responseBody);

                int providerHttpStatus = root.path("status").asInt(httpStatus);
                String providerDetail = root.path("message").asText(detail);

                log.info(
                        "Provider error response parsed: exchangeId={}, previousHttpStatus={}, providerHttpStatus={}",
                        exchange.getExchangeId(),
                        httpStatus,
                        providerHttpStatus);

                httpStatus = providerHttpStatus;
                detail = providerDetail;
            } catch (Exception parseException) {
                log.warn(
                        "Failed to parse provider JSON error response; ResourceBundle values will be used: exchangeId={}, providerStatus={}",
                        exchange.getExchangeId(),
                        exception.getStatusCode().value(),
                        parseException);
            }
        }

        exchange.getMessage().setHeader(
                Exchange.HTTP_RESPONSE_CODE,
                httpStatus);

        log.info(
                "HTTP response code set for legacy fault: exchangeId={}, httpStatus={}",
                exchange.getExchangeId(),
                httpStatus);

        HttpStatus responseHttpStatus = HttpStatus.resolve(httpStatus);
        String httpCode = responseHttpStatus != null
                ? responseHttpStatus.name()
                : String.valueOf(httpStatus);

        return new LegacyMbFaultResponse(
                detail,
                httpStatus,
                responseStatus.name(),
                message,
                httpCode);
    }

    private Locale resolveRequestLocale(Exchange exchange) {
        String acceptLanguage = exchange.getMessage()
                .getHeader("Accept-Language", String.class);

        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            Locale defaultLocale =
                    AccessibleLocale.DEFAULT_LOCALE.getLocale();

            log.info(
                    "Accept-Language is missing; using default locale: exchangeId={}, locale={}",
                    exchange.getExchangeId(),
                    defaultLocale);

            return defaultLocale;
        }

        log.info(
                "Resolving request locale: exchangeId={}, acceptLanguage={}",
                exchange.getExchangeId(),
                acceptLanguage);

        for (String language : acceptLanguage.split(",")) {
            String languageTag = language
                    .split(";", 2)[0]
                    .trim();

            Optional<AccessibleLocale> locale =
                    AccessibleLocale.findByLocale(
                            Locale.forLanguageTag(languageTag));

            if (locale.isPresent()) {
                log.info(
                        "Request locale resolved: exchangeId={}, languageTag={}, locale={}",
                        exchange.getExchangeId(),
                        languageTag,
                        locale.get().getLocale());

                return locale.get().getLocale();
            }
        }

        Locale defaultLocale =
                AccessibleLocale.DEFAULT_LOCALE.getLocale();

        log.info(
                "No supported locale found; using default: exchangeId={}, acceptLanguage={}, locale={}",
                exchange.getExchangeId(),
                acceptLanguage,
                defaultLocale);

        return defaultLocale;
    }

    private String resolveBundleKey(Error error) {
        String bundleKey;
        if (error.getException() == null) {
            bundleKey = EXCEPTION_BUNDLE_DEFAULT_KEY;
        } else {
            bundleKey = EXCEPTION_BUNDLE_DEFAULT_PREFIX
                    + error.getException().getClass().getName();
            if (error.getException() instanceof CardException) {
                String scmErrorCode = error.getErrorCode();
                if (scmErrorCode != null && !scmErrorCode.isBlank()) {
                    bundleKey += ":" + removeScmPrefix(scmErrorCode);
                }
            }
        }

        log.info(
                "Exception bundle key resolved: exceptionClass={}, bundleKey={}",
                error.getException() != null
                        ? error.getException().getClass().getName()
                        : null,
                bundleKey);

        return bundleKey;
    }

    private String removeScmPrefix(String errorCode) {
        return errorCode.startsWith("SCM-")
                ? errorCode.substring("SCM-".length())
                : errorCode;
    }

    private String resolveBundleMessage(
            Locale locale,
            String key,
            String fallback) {

        Optional<String> message =
                resourceBundleService.get(locale, key);

        if (message.isPresent()) {
            log.info(
                    "Bundle message found: locale={}, key={}",
                    locale,
                    key);

            return message.get();
        }

        Optional<String> defaultMessage =
                resourceBundleService.get(
                        locale,
                        EXCEPTION_BUNDLE_DEFAULT_KEY);

        if (defaultMessage.isPresent()) {
            log.info(
                    "Specific bundle message not found; default message used: locale={}, requestedKey={}, defaultKey={}",
                    locale,
                    key,
                    EXCEPTION_BUNDLE_DEFAULT_KEY);

            return defaultMessage.get();
        }

        log.warn(
                "Bundle message not found; fallback value used: locale={}, key={}",
                locale,
                key);

        return fallback;
    }

}
