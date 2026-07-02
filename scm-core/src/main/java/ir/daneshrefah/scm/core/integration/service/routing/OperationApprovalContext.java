package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;

public record OperationApprovalContext(
        Service service,
        ServiceOperation serviceOperation,
        Definition approvalDefinition,
        Message message
) {
}
