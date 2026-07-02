package ir.daneshrefah.scm.core.integration.service.routing;

import org.springframework.stereotype.Component;

@Component
public class DefaultOperationApprovalPolicy implements OperationApprovalPolicy {
    public static final String CODE = "DEFAULT";

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean isApproved(OperationApprovalContext context) {
        return context != null && context.message() != null && context.message().isSuccessful();
    }
}
