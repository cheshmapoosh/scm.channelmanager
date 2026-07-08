package ir.daneshrefah.scm.provider.task.camel;

import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngine;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowEngineRegistry;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRole;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRoleResolver;
import org.apache.camel.Exchange;

public class TaskProviderOperationAdapter {
    private final TaskWorkflowRoleResolver roleResolver;
    private final TaskWorkflowEngineRegistry engineRegistry;

    public TaskProviderOperationAdapter(
            TaskWorkflowRoleResolver roleResolver,
            TaskWorkflowEngineRegistry engineRegistry
    ) {
        this.roleResolver = roleResolver;
        this.engineRegistry = engineRegistry;
    }

    public String endpointProviderCode(String remaining) {
        if (roleResolver.isDeprecatedOperationCodeAlias(remaining)) {
            return engineRegistry.requireProviderCode(
                    TaskWorkflowEngineRegistry.INTERNAL_PROVIDER_CODE
            );
        }
        return engineRegistry.requireProviderCode(remaining);
    }

    public void execute(String providerCode, Exchange exchange) {
        TaskWorkflowRole role = roleResolver.resolve(providerCode, exchange);
        TaskWorkflowEngine engine = engineRegistry.engineFor(providerCode);
        if (!engine.supports(role)) {
            throw new IllegalArgumentException("scm-task:" + providerCode
                    + " engine-type=" + engine.engineType()
                    + " does not support TaskWorkflowRole=" + role);
        }
        exchange.getMessage().setBody(engine.execute(role, exchange));
    }
}
