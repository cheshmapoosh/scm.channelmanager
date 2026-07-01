package ir.daneshrefah.scm.core.integration.gateway.contract;

import ir.daneshrefah.scm.common.model.ScmResponse;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.core.integration.inbound.rest.HttpStatusMapper;
import org.apache.camel.Exchange;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("restProblemDetailFaultEncoder")
public class RestProblemDetailFaultEncoder implements FaultContractEncoder {
    @Override
    public Object encode(Exchange exchange, ScmFault fault, ClientContract contract) {
        Error error = firstError(fault);
        int httpStatus = httpStatus(fault, error);
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, httpStatus);

        MessageStatus status = error != null ? error.getStatus() : null;
//        ProblemDetail problemDetail = ProblemDetail.forStatus(httpStatus);
//        problemDetail.setTitle("SCM request failed");
//        problemDetail.setDetail(error != null ? error.getMessage() : "Request failed.");
//        if (error != null) {
//            problemDetail.setProperty("code", error.getErrorCode());
//            problemDetail.setProperty("messageKey", error.getSource());
//            problemDetail.setProperty("status", error.getStatus());
//        }
//        problemDetail.setProperty("contract", contract.name());
        return ScmResponse
                .builder()
                .errors(List.of(error))
                .status(status != null ? status : MessageStatus.SC_ERROR_SYSTEM)
                .build();
    }

    private Error firstError(ScmFault fault) {
        return fault != null && fault.getErrors() != null && !fault.getErrors().isEmpty()
                ? fault.getErrors().getFirst()
                : null;
    }

    private int httpStatus(ScmFault fault, Error error) {
        Integer httpStatus = error != null ? HttpStatusMapper.toHttpStatus(error.getStatus()) : null;
        if (httpStatus == null && fault != null) {
            httpStatus = HttpStatusMapper.toHttpStatus(fault.getStatus());
        }
        return httpStatus != null ? httpStatus : 500;
    }
}
