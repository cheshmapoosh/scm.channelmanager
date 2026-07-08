package ir.daneshrefah.scm.core.integration.service.routing;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;

public record OperationApprovalContext(
        Service service,
        ServiceOperation serviceOperation,
        Definition approvalDefinition,
        JsonNode requestBody
) {
}
