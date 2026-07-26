package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.utility.lock.LockUtility;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionPolicyRegistry;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingEngineRegistry;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingOperationMetadataResolver;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationEndpointResolver;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationSelector;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowProviderCapability;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRecoveryStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Isolates every direct reference to the optional task-provider API. The class
 * is skipped before reflective bean-method inspection when the provider API is
 * absent.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(
        name = "ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRecoveryStore"
)
public class TaskProviderCoreIntegrationConfiguration {

    @Bean
    TaskWorkflowSnapshotMapper taskWorkflowSnapshotMapper(
            ObjectMapper objectMapper
    ) {
        return new TaskWorkflowSnapshotMapper(objectMapper);
    }

    @Bean
    RoutingRecoveryPolicyRegistry routingRecoveryPolicyRegistry() {
        return new RoutingRecoveryPolicyRegistry(List.of(
                new FirstRoutingRecoveryPolicy(),
                new ChainOnApproveRoutingRecoveryPolicy()
        ));
    }

    @Bean
    TaskWorkflowProviderCapabilityRegistry
    taskWorkflowProviderCapabilityRegistry(
            ObjectProvider<TaskWorkflowProviderCapability> capabilities
    ) {
        return new TaskWorkflowProviderCapabilityRegistry(
                capabilities.orderedStream().toList()
        );
    }

    @Bean
    TaskWorkflowDistributedLock taskWorkflowDistributedLock(
            ObjectProvider<LockUtility> lockProvider,
            ObjectProvider<CacheClientProperties> propertiesProvider
    ) {
        return new TaskWorkflowDistributedLock(
                lockProvider,
                propertiesProvider
        );
    }

    @Bean
    TaskWorkflowRoutePlanFactory taskWorkflowRoutePlanFactory(
            ServiceOperationSelector operationSelector,
            ServiceOperationEndpointResolver endpointResolver,
            TaskWorkflowActionPlanParser actionPlanParser,
            TaskWorkflowCommandPlanValidator validator,
            RoutingDecisionPolicyRegistry policyRegistry,
            TaskWorkflowPayloadMapper payloadMapper,
            RoutingOperationMetadataResolver operationMetadataResolver,
            TaskWorkflowPlanFingerprint fingerprint,
            TaskWorkflowProviderCapabilityRegistry providerCapabilities
    ) {
        return new TaskWorkflowRoutePlanFactory(
                operationSelector,
                endpointResolver,
                actionPlanParser,
                validator,
                policyRegistry,
                payloadMapper,
                operationMetadataResolver,
                fingerprint,
                providerCapabilities
        );
    }

    @Bean
    TaskWorkflowExecutionCoordinator taskWorkflowExecutionCoordinator(
            ObjectProvider<TaskWorkflowRecoveryStore> storeProvider,
            RoutingEngineRegistry engineRegistry,
            RoutingRecoveryPolicyRegistry recoveryPolicyRegistry,
            TaskWorkflowExecutionIdentityResolver executionIdentityResolver,
            TaskWorkflowInputResolver inputResolver,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowSnapshotMapper snapshotMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator,
            TaskWorkflowDistributedLock distributedLock
    ) {
        return new TaskWorkflowExecutionCoordinator(
                storeProvider,
                engineRegistry,
                recoveryPolicyRegistry,
                executionIdentityResolver,
                inputResolver,
                payloadMapper,
                snapshotMapper,
                transactionCoordinator,
                distributedLock
        );
    }

    @Bean
    TaskWorkflowRuntime taskWorkflowRuntime(
            TaskWorkflowRoutePlanFactory routePlanFactory,
            TaskWorkflowExecutionCoordinator executionCoordinator
    ) {
        return new TaskProviderTaskWorkflowRuntime(
                routePlanFactory,
                executionCoordinator
        );
    }
}
