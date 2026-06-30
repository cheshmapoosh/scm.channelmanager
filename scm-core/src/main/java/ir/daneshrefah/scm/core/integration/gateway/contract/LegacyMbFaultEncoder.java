package ir.daneshrefah.scm.core.integration.gateway.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.FailResponse;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component("legacyMbFaultEncoder")
@RequiredArgsConstructor
public class LegacyMbFaultEncoder implements FaultContractEncoder {
    private final ObjectMapper objectMapper;

    @Override
    public Object encode(Exchange exchange, ScmFault fault, ClientContract contract) {
        Error error = fault.getErrors().getFirst();
        Integer code = extractCode(error.getErrorCode());
        Integer httpStatus = HttpStatusMapper.toHttpStatus(error.getStatus());
        if (httpStatus == null) {
            httpStatus = 500;
        }

        String detail = error.getMessageFa();
        String errorText = error.getException() != null ? error.getException().getMessage() : null;
        if (error.getException() instanceof HttpClientErrorException ex) {
            try {
                JsonNode root = objectMapper.readTree(ex.getResponseBodyAsString());
                httpStatus = root.path("status").asInt(httpStatus);
                detail = root.path("message").asText(detail);
            } catch (Exception ignored) {
            }
        }

        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpStatus);
        return FailResponse.builder()
                .status(httpStatus)
                .code(code != null ? code : httpStatus)
                .title("error")
                .detail(detail)
                .error(errorText)
                .message(error.getMessage())
                .messageKey(error.getSource())
                .build();
    }

    private Integer extractCode(String errorCode) {
        try {
            if (errorCode == null) {
                return 0;
            }
            return Integer.parseInt(errorCode.contains("-") ? errorCode.split("-")[1] : errorCode);
        } catch (Exception e) {
            return null;
        }
    }
}
