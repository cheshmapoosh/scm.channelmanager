package ir.daneshrefah.scm.core.integration.gateway.contract;

import ir.daneshrefah.scm.common.model.ScmResponse;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component("jsonScmResponseEncoder")
public class JsonScmResponseEncoder implements ResponseContractEncoder {
    @Override
    public Object encode(Exchange exchange, ClientContract contract) {
        Object body = exchange.getMessage().getBody();
        if (body instanceof Message message) {
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatusMapper.toHttpStatus(message.getStatus()));
            return ScmResponse.builder()
                    .status(message.getStatus())
                    .result(message.getPayload())
                    .errors(message.getErrors())
                    .build();
        }
        if (body instanceof ScmFault scmFault) {
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, HttpStatusMapper.toHttpStatus(scmFault.getStatus()));
            return ScmResponse.builder()
                    .status(scmFault.getStatus())
                    .result(null)
                    .errors(scmFault.getErrors())
                    .build();
        }
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
        return ScmResponse.builder()
                .status(MessageStatus.SC_SUCCESS)
                .result(body)
                .errors(null)
                .build();
    }
}
