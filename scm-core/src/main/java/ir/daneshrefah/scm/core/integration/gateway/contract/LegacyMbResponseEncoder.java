package ir.daneshrefah.scm.core.integration.gateway.contract;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component("legacyMbResponseEncoder")
@Slf4j
public class LegacyMbResponseEncoder implements ResponseContractEncoder {

    @Override
    public Object encode(Exchange exchange, ClientContract contract) {
        try {
            Object body = exchange.getMessage().getBody();

            if (body instanceof Message message) {
                exchange.getMessage().setHeader(
                        Exchange.HTTP_RESPONSE_CODE,
                        HttpStatusMapper.toHttpStatus(message.getStatus())
                );
                return body;
            }

            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
            return body;
        } catch (Exception e) {
            log.error("Could not encode legacy MB response", e);
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 500);
            return exchange.getMessage().getBody();
        }
    }
}
