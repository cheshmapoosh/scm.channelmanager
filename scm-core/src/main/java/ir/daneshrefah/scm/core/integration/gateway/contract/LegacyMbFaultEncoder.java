package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.model.FailResponse;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
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
    private final ObjectMapper objectMapper;
    private final ResourceBundleService resourceBundleService;

    @Override
    public Object encode(Exchange exchange, ScmFault fault, ClientContract contract) {
        try {
            return encodeFault(exchange, fault);
        } catch (Exception e) {
            log.error("Could not encode legacy MB fault", e);
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            return FailResponse.builder()
                    .status(500)
                    .code(500)
                    .title("error")
                    .error("INTERNAL_SERVER_ERROR")
                    .build();
        }
    }

    private FailResponse encodeFault(Exchange exchange, ScmFault fault) {
        Error error = fault.getErrors().getFirst();
        Integer code = extractCode(error.getErrorCode());
        Integer httpStatus = Optional.ofNullable(HttpStatusMapper.toHttpStatus(error.getStatus())).orElse(500);
        Locale requestLocale = resolveRequestLocale(exchange);
        String bundleKey = resolveBundleKey(error);
        String message = resolveBundleMessage(requestLocale, bundleKey, error.getMessage());
        String detail = resolveBundleMessage(AccessibleLocale.FA_IR.getLocale(), bundleKey, error.getMessageFa());
        String errorText = error.getException() != null ? error.getException().getMessage() : null;

        if (error.getException() instanceof HttpClientErrorException ex) {
            try {
                JsonNode root = objectMapper.readTree(ex.getResponseBodyAsString());
                httpStatus = root.path("status").asInt(httpStatus);
                detail = root.path("message").asText(detail);
            } catch (Exception ignored) {
                // Keep the ResourceBundle values when the provider body is not valid JSON.
            }
        }

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpStatus);
        return FailResponse.builder()
                .status(httpStatus)
                .code(code != null ? code : httpStatus)
                .title("error")
                .detail(detail)
                .error(errorText)
                .message(message)
                .messageKey(bundleKey)
                .build();
    }

    private Locale resolveRequestLocale(Exchange exchange) {
        String acceptLanguage = exchange.getMessage().getHeader("Accept-Language", String.class);
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return AccessibleLocale.DEFAULT_LOCALE.getLocale();
        }
        for (String language : acceptLanguage.split(",")) {
            String languageTag = language.split(";", 2)[0].trim();
            Optional<AccessibleLocale> locale = AccessibleLocale.findByLocale(Locale.forLanguageTag(languageTag));
            if (locale.isPresent()) {
                return locale.get().getLocale();
            }
        }
        return AccessibleLocale.DEFAULT_LOCALE.getLocale();
    }

    private String resolveBundleKey(Error error) {
        return error.getException() == null
                ? EXCEPTION_BUNDLE_DEFAULT_KEY
                : EXCEPTION_BUNDLE_DEFAULT_PREFIX + error.getException().getClass().getName();
    }

    private String resolveBundleMessage(Locale locale, String key, String fallback) {
        return resourceBundleService.get(locale, key)
                .or(() -> resourceBundleService.get(locale, EXCEPTION_BUNDLE_DEFAULT_KEY))
                .orElse(fallback);
    }

    private Integer extractCode(String errorCode) {
        try {
            if (errorCode == null) {
                return 0;
            }
            return Integer.parseInt(errorCode.contains("-") ? errorCode.split("-", 2)[1] : errorCode);
        } catch (Exception e) {
            return null;
        }
    }
}
