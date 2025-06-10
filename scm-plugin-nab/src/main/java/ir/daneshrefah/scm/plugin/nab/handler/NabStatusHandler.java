package ir.daneshrefah.scm.plugin.nab.handler;

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
        Language jsonpath = exchange.getContext().resolveLanguage("jsonpath");
        String pError = jsonpath
                .createExpression("$.header.P_ERROR")
                .evaluate(exchange, String.class);

        String pMessage = jsonpath
                .createExpression("$.header.P_MESSAGE")
                .evaluate(exchange, String.class);

        String pStack = jsonpath
                .createExpression("$.header.P_STACK")
                .evaluate(exchange, String.class);
        if (!Objects.equals(SUCCESS, pError)) {
            throw new ScmException(pError, pMessage);
        }

    }
}
