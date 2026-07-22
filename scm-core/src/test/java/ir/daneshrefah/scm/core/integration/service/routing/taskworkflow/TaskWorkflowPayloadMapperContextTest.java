package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskWorkflowPayloadMapperContextTest {

    @Test
    void businessEnvelopeCarriesStableApproveContextAndAllEarlierStepResults() {
        ObjectMapper objectMapper = new ObjectMapper();
        TaskWorkflowPayloadMapper mapper = new TaskWorkflowPayloadMapper(objectMapper);
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setBody(objectMapper.createObjectNode().put("request", "original"));
        exchange.setProperty(Message.CORRELATION_ID, "paymaster-correlation");
        RoutingExecutionContext context = new RoutingExecutionContext(
                exchange.getMessage().getBody()
        );
        ObjectNode transactionData = objectMapper.createObjectNode()
                .put("companyId", 42L)
                .put("paymasterUserId", 77L);
        ObjectNode approveResponse = objectMapper.createObjectNode()
                .put("processId", 98541L)
                .set("transactionData", transactionData);
        mapper.rememberApproveContext(approveResponse, context);
        context.record("approve-paymaster-process-for-execution", approveResponse);

        ObjectNode nabRequest = (ObjectNode) mapper.toRequest(
                exchange,
                TaskWorkflowRole.BUSINESS_OPERATION,
                context
        );

        assertEnvelope(nabRequest);
        assertEquals(98541L, exchange.getProperty(
                TaskWorkflowExchangeProperties.PROCESS_ID,
                Long.class
        ));
        assertEquals(98541L, context.processId());
        assertEquals("paymaster-correlation", context.correlationId());
        assertEquals(98541L, nabRequest.path("stepResults")
                .path("approve-paymaster-process-for-execution")
                .path("processId")
                .asLong());

        ((ObjectNode) nabRequest.path("transactionData")).put("companyId", -1L);
        ObjectNode nabResult = objectMapper.createObjectNode()
                .put("registrationId", "nab-registration-id");
        context.record("register-paymaster-in-nab", nabResult);

        ObjectNode permissionRequest = (ObjectNode) mapper.toRequest(
                exchange,
                TaskWorkflowRole.BUSINESS_OPERATION,
                context
        );

        assertEnvelope(permissionRequest);
        assertEquals("nab-registration-id", permissionRequest.path("stepResults")
                .path("register-paymaster-in-nab")
                .path("registrationId")
                .asText());
        assertEquals(2, permissionRequest.path("stepResults").size());
    }

    private void assertEnvelope(ObjectNode request) {
        assertEquals(98541L, request.path("processId").asLong());
        assertEquals("paymaster-correlation", request.path("correlationId").asText());
        assertEquals(42L, request.path("transactionData").path("companyId").asLong());
        assertEquals(77L, request.path("transactionData").path("paymasterUserId").asLong());
    }
}
