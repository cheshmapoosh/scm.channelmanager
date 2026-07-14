package ir.daneshrefah.scm.plugin.nab.handler;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.handler.StatusHandler;
import org.apache.camel.Exchange;
import org.apache.camel.spi.Language;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class NabStatusHandler implements StatusHandler {
    private final static String SUCCESS = "0";

    @Override
    public void handle(Exchange exchange) {
        JsonNode jsonBody = exchange.getIn().getBody(JsonNode.class);
        JsonNode body = jsonBody.get("body");

        if (Objects.isNull(body)) {
            return;
        }

        JsonNode pError = body.get("P_ERROR");
        JsonNode pMsg = body.get("P_MESSAGE");
        JsonNode pStack = body.get("P_STACK");

//        Language jsonpath = exchange.getContext().resolveLanguage("jsonpath");
//        String pError = jsonpath
//                .createExpression("$.header.P_ERROR")
//                .evaluate(exchange, String.class);
//
//        String pMessage = jsonpath
//                .createExpression("$.header.P_MESSAGE")
//                .evaluate(exchange, String.class);
//
//        String pStack = jsonpath
//                .createExpression("$.header.P_STACK")
//                .evaluate(exchange, String.class);
        if (pError == null || pMsg == null) {
            return;
        }
        if (!Objects.equals(SUCCESS, pError)) {
            throw new ScmException(pError.asText(), pMsg.asText());
        }

    }
}
