package ir.daneshrefah.scm.core.integration.service.routing.customOperationApprovalPolicy;

import ir.daneshrefah.scm.common.data.constant.ActionCode;
import ir.daneshrefah.scm.core.integration.service.routing.OperationApprovalContext;
import ir.daneshrefah.scm.core.integration.service.routing.OperationApprovalPolicy;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KarpardazPolicy implements OperationApprovalPolicy {
    public static final String CODE = "SUCCESSFUL";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean isApproved(OperationApprovalContext context) {
        ActionCode actionCode = ActionCode.findByCode(context.payload().get("actionCode").asText());
        return context != null && context.payload() != null && (Objects.isNull(actionCode) ? false : actionCode.getName().equals(CODE));
    }
}
