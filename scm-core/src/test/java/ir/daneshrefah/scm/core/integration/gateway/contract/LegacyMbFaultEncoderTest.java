package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.exception.CardException;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LegacyMbFaultEncoderTest {

    private static final String BUNDLE_KEY = "ex::java.lang.IllegalStateException";
    private final ResourceBundleService resourceBundleService = mock(ResourceBundleService.class);
    private final LegacyMbFaultEncoder faultEncoder =
            new LegacyMbFaultEncoder(new ObjectMapper(), resourceBundleService);
    private final ClientContract contract = new ClientContract(
            "rest-legacy-v1",
            "legacyMbRequestDecoder",
            "legacyMbResponseEncoder",
            "legacyMbFaultEncoder"
    );

    @Test
    void legacyFaultEncoderMapsResourceBundleToFailResponse() {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setHeader("Accept-Language", "en-US");
        when(resourceBundleService.get(new Locale("en", "US"), BUNDLE_KEY))
                .thenReturn(Optional.of("Database English message"));
        when(resourceBundleService.get(new Locale("fa", "IR"), BUNDLE_KEY))
                .thenReturn(Optional.of("پیام فارسی دیتابیس"));
        Error error = new Error(
                "shetab",
                1017,
                "Provider connection lost",
                "ارتباط با سرویس‌دهنده قطع شد",
                MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER,
                new IllegalStateException("Shetab connection lost after send")
        );
        ScmFault fault = ScmFault.builder()
                .status(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER)
                .errors(java.util.List.of(error))
                .build();
        Object encoded = faultEncoder.encode(exchange, fault, contract);

        assertThat(encoded).isInstanceOf(LegacyMbFaultResponse.class);
        LegacyMbFaultResponse response = (LegacyMbFaultResponse) encoded;
        assertThat(response.code()).isEqualTo(502);
        assertThat(response.text()).isEqualTo("Database English message");
        assertThat(response.detail()).isEqualTo("پیام فارسی دیتابیس");
        assertThat(response.messageKey()).isEqualTo(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER.name());
        assertThat(response.httpCode()).isEqualTo("BAD_GATEWAY");
        assertThat(exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class))
                .isEqualTo(502);
    }

    @Test
    void returnsFailResponseWhenFaultMappingFails() {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        ScmFault malformedFault = ScmFault.builder()
                .status(MessageStatus.SC_ERROR_SYSTEM)
                .build();
        Object encoded = faultEncoder.encode(exchange, malformedFault, contract);

        assertThat(encoded).isInstanceOf(LegacyMbFaultResponse.class);
        LegacyMbFaultResponse response = (LegacyMbFaultResponse) encoded;
        assertThat(response.code()).isEqualTo(500);
        assertThat(response.messageKey()).isEqualTo(MessageStatus.SC_ERROR_SYSTEM.name());
        assertThat(response.httpCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    void mapsIncorrectPinToLegacySecurityViolationContract() throws Exception {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        CardException exception = new CardException("117", "117", "incorrect pin");
        String pinBundleKey = "ex::ir.daneshrefah.scm.common.exception.CardException:1171700";
        when(resourceBundleService.get(new Locale("en", "US"), pinBundleKey))
                .thenReturn(Optional.of("incorrect pin"));
        when(resourceBundleService.get(new Locale("fa", "IR"), pinBundleKey))
                .thenReturn(Optional.of("نام کاربری یا گذرواژه اشتباه است"));
        Error error = new Error(
                null,
                1171700,
                "Unknown error",
                "خطای ناشناخته",
                MessageStatus.INCORRECT_PIN,
                exception);
        ScmFault fault = ScmFault.builder()
                .status(MessageStatus.INCORRECT_PIN)
                .errors(java.util.List.of(error))
                .build();

        LegacyMbFaultResponse response = (LegacyMbFaultResponse) faultEncoder.encode(exchange, fault, contract);

        assertThat(new ObjectMapper().writeValueAsString(response)).isEqualTo(
                "{\"detail\":\"نام کاربری یا گذرواژه اشتباه است\",\"code\":406,\"messageKey\":\"SECURITY_VIOLATION\",\"text\":\"incorrect pin\",\"httpCode\":\"NOT_ACCEPTABLE\"}");
        assertThat(exchange.getMessage().getHeader(Exchange.HTTP_RESPONSE_CODE, Integer.class))
                .isEqualTo(406);
    }
}
