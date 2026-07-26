package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import org.apache.camel.Exchange;

public interface TaskWorkflowRouteIdentityResolver {

    boolean supports(ProtocolType protocol);

    TaskWorkflowRouteIdentity resolve(Exchange exchange);
}
