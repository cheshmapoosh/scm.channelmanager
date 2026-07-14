package ir.daneshrefah.scm.logging.utils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import io.opentelemetry.api.trace.Span;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationProvider;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.utils.constant.Constants;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class LegacyGatewayLogSpanEnricherTest {

    private static final String RAW_SOURCE_PAN = "6037991111111111";
    private static final String RAW_DESTINATION_PAN = "5894632222222222";

    private final LegacyGatewayLogSpanEnricher enricher = new LegacyGatewayLogSpanEnricher();

    @Test
    void enrichesGatewayRequestForTargetCardServices() {
        for (String serviceCode : new String[]{"cardInquiry", "cardPasswordInquiry", "cardXferAdd"}) {
            Exchange exchange = exchange("""
                    {
                      "fundTransfer": {
                        "sourceCardNumber": "6037991111111111",
                        "destinationCardNumber": "5894632222222222",
                        "sourceAccountNumber": "123456",
                        "amount": "5000"
                      }
                    }
                    """);
            exchange.getMessage().setHeader("X-Forwarded-For", "10.10.10.1, 10.10.10.2");
            Span span = mock(Span.class);

            enricher.enrichGatewayRequest(exchange, service(serviceCode), span, "message-1");

            assertThat(exchange.getProperty(LegacyGatewayLogSpanEnricher.GATEWAY_SPAN_PROPERTY)).isSameAs(span);
            verify(span).setAttribute("scm.log.origin", "SCM_WEB");
            verify(span).setAttribute("scm.log.bridge.version", "8.5.4");
            verify(span).setAttribute("scm.log.record.type", "GATEWAY_TRANSACTION");
            verify(span).setAttribute(LogAttribute.CARD_NO.getAttributeName(), RAW_SOURCE_PAN);
            verify(span).setAttribute(LogAttribute.DESTINATION.getAttributeName(), RAW_DESTINATION_PAN);
            verify(span).setAttribute(LogAttribute.ACCOUNT_NO.getAttributeName(), "123456");
            verify(span).setAttribute(LogAttribute.AMOUNT.getAttributeName(), "5000");
            verify(span).setAttribute(LogAttribute.INTER_BANK.getAttributeName(), "true");
            verify(span).setAttribute(LogAttribute.CORRELATION_ID.getAttributeName(), "message-1");
            verify(span).setAttribute(LogAttribute.CLIENT_IP_ADDRESS.getAttributeName(), "10.10.10.1");
            verify(span).setAttribute(LogAttribute.TRANSACTION_TYPE_REQUEST.getAttributeName(), 1L);
            verify(span).setAttribute("requestLogStatus", "REQUEST_TO_CHANNEL");
        }
    }

    @Test
    void providerSuccessIsCopiedToGatewayResponse() {
        Exchange exchange = exchange("{}");
        Span span = mock(Span.class);
        enricher.enrichGatewayRequest(exchange, service("cardXferAdd"), span, "message-1");

        exchange.getMessage().setBody(Map.of("fields", Map.of("39", "00", "37", "123456789012", "56", "original-rrn")));
        enricher.captureProviderResponse(exchange, shetabProviderOperation());
        exchange.getMessage().setBody(Message.builder()
                .status(MessageStatus.SC_SUCCESS)
                .payload(JsonNodeFactory.instance.objectNode().put("cardNo", RAW_SOURCE_PAN))
                .build());

        enricher.enrichGatewayResponse(exchange, service("cardXferAdd"), span);

        verify(span).setAttribute("serverCode", "00");
        verify(span).setAttribute(LogAttribute.DOC_NO.getAttributeName(), "123456789012");
        verify(span).setAttribute(LogAttribute.EXTERNAL_SEQUENCE_ID.getAttributeName(), "123456789012");
        verify(span).setAttribute(LogAttribute.ORIGINAL_SEQUENCE_ID.getAttributeName(), "original-rrn");
        verify(span).setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), 2L);
        verify(span).setAttribute("responseLogStatus", "RESPONSE_FROM_CHANNEL");
        verify(span).setAttribute(LogAttribute.STATUS_CODE.getAttributeName(), MessageStatus.SC_SUCCESS.getCode());
        verify(span).setAttribute(eq(LogAttribute.MESSAGE_RESPONSE.getAttributeName()), contains(RAW_SOURCE_PAN));
    }

    @Test
    void providerBusinessFailureStillCountsAsGatewayResponse() {
        Exchange exchange = exchange("{}");
        Span span = mock(Span.class);
        enricher.enrichGatewayRequest(exchange, service("cardPasswordInquiry"), span, "message-1");

        exchange.getMessage().setBody(Map.of("fields", Map.of("39", "51", "37", "222222222222")));
        enricher.captureProviderResponse(exchange, shetabProviderOperation());
        exchange.getMessage().setBody(Message.builder()
                .status(MessageStatus.NO_SUFFICIENT_FUNDS)
                .payload(JsonNodeFactory.instance.objectNode())
                .build());

        enricher.enrichGatewayResponse(exchange, service("cardPasswordInquiry"), span);

        verify(span).setAttribute("serverCode", "51");
        verify(span).setAttribute(LogAttribute.DOC_NO.getAttributeName(), "222222222222");
        verify(span).setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), 2L);
        verify(span).setAttribute("responseLogStatus", "RESPONSE_FROM_CHANNEL");
        verify(span).setAttribute(LogAttribute.STATUS_CODE.getAttributeName(), MessageStatus.NO_SUFFICIENT_FUNDS.getCode());
    }

    @Test
    void timeoutWithoutProviderResponseDoesNotCreateResponseAttributes() {
        Exchange exchange = exchange("{}");
        Span span = mock(Span.class);
        enricher.enrichGatewayRequest(exchange, service("cardInquiry"), span, "message-1");
        RuntimeException timeout = new RuntimeException("timeout waiting for provider");
        exchange.setProperty(Exchange.EXCEPTION_CAUGHT, timeout);
        exchange.getMessage().setBody(ScmFault.builder()
                .status(MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER)
                .title("timeout")
                .build());

        enricher.enrichGatewayResponse(exchange, service("cardInquiry"), span);

        verify(span, never()).setAttribute(eq("serverCode"), anyString());
        verify(span, never()).setAttribute(eq(LogAttribute.MESSAGE_RESPONSE.getAttributeName()), anyString());
        verify(span, never()).setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), 2L);
        verify(span).setAttribute(LogAttribute.STATUS_CODE.getAttributeName(), MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER.getCode());
        verify(span).setAttribute(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName(), RuntimeException.class.getName());
        verify(span).setAttribute(LogAttribute.DESCRIPTION.getAttributeName(), "timeout waiting for provider");
    }

    @Test
    void unparsableProviderResponseIsStillAResponseWithFailedStatus() {
        Exchange exchange = exchange("{}");
        Span span = mock(Span.class);
        enricher.enrichGatewayRequest(exchange, service("cardInquiry"), span, "message-1");

        exchange.getMessage().setBody("not-json");
        enricher.captureProviderResponse(exchange, shetabProviderOperation());
        exchange.getMessage().setBody(Message.builder()
                .status(MessageStatus.SC_ERROR_SYSTEM)
                .payload(JsonNodeFactory.instance.objectNode())
                .build());

        enricher.enrichGatewayResponse(exchange, service("cardInquiry"), span);

        verify(span, never()).setAttribute(eq("serverCode"), anyString());
        verify(span).setAttribute(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName(), 2L);
        verify(span).setAttribute("responseLogStatus", "RESPONSE_FAILED");
        verify(span).setAttribute(LogAttribute.STATUS_CODE.getAttributeName(), MessageStatus.SC_ERROR_SYSTEM.getCode());
    }

    @Test
    void technicalSpansAreTaggedWithoutGatewayRecordType() {
        Span operationSpan = mock(Span.class);
        Span providerSpan = mock(Span.class);
        Span customizerSpan = mock(Span.class);

        enricher.markScmWebSpan(operationSpan);
        enricher.markScmWebSpan(providerSpan);
        enricher.markScmWebSpan(customizerSpan);

        for (Span span : new Span[]{operationSpan, providerSpan, customizerSpan}) {
            verify(span).setAttribute("scm.log.origin", "SCM_WEB");
            verify(span).setAttribute("scm.log.bridge.version", "8.5.4");
            verify(span, never()).setAttribute(eq("scm.log.record.type"), anyString());
        }
    }

    @Test
    void nonShetabProviderOutputIsIgnored() {
        Exchange exchange = exchange(Map.of("fields", Map.of("39", "00", "37", "123456789012")));
        Operation operation = new Operation();
        operation.setType(OperationType.REST);

        enricher.captureProviderResponse(exchange, operation);

        assertThat(exchange.getProperty(LegacyGatewayLogSpanEnricher.PROVIDER_RESPONSE_RECEIVED_PROPERTY)).isNull();
    }

    @Test
    void nonGatewayExchangeIsIgnored() {
        Exchange exchange = bareExchange("{}");
        Span span = mock(Span.class);

        enricher.enrichGatewayRequest(exchange, service("cardInquiry"), span, "message-1");

        assertThat(exchange.getProperty(LegacyGatewayLogSpanEnricher.GATEWAY_SPAN_PROPERTY)).isNull();
        verify(span, never()).setAttribute(eq("scm.log.origin"), anyString());
        verify(span, never()).setAttribute(eq("scm.log.record.type"), anyString());
    }

    @Test
    void clientIpDoesNotUseHttpReferer() {
        Exchange exchange = exchange("{}");
        exchange.getMessage().setHeader(Constants.CAMEL_PARAMETER_REFERER, "https://client.example/login");

        assertThat(enricher.clientIpAddress(exchange)).isBlank();
    }

    @Test
    void clientIpFallsBackToRemoteAddress() {
        Exchange exchange = exchange("{}");
        exchange.getMessage().setHeader(Constants.CAMEL_PARAMETER_HTTP_REMOTE_ADDRESS, "/10.20.30.40:45678");

        assertThat(enricher.clientIpAddress(exchange)).isEqualTo("10.20.30.40");
    }

    private Exchange exchange(Object body) {
        Exchange exchange = bareExchange(body);
        exchange.setProperty(Message.GATEWAY_NAME, "scm-web");
        return exchange;
    }

    private Exchange bareExchange(Object body) {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setBody(body);
        return exchange;
    }

    private Service service(String code) {
        Service service = new Service();
        service.setCode(code);
        return service;
    }

    private Operation shetabProviderOperation() {
        OperationProvider provider = new OperationProvider();
        provider.setName("shetab");
        provider.setUri("scm-shetab:hps-shetab7");
        Operation operation = new Operation();
        operation.setType(OperationType.PROVIDER);
        operation.setProvider(provider);
        return operation;
    }
}
