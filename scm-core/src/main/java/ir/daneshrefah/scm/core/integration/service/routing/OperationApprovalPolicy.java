package ir.daneshrefah.scm.core.integration.service.routing;

public interface OperationApprovalPolicy {
    String code();

    boolean isApproved(OperationApprovalContext context);
}
