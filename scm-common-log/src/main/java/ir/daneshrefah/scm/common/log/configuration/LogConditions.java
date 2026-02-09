package ir.daneshrefah.scm.common.log.configuration;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * LogConditions is a container for conditional logic that determines
 * whether certain features or services should be enabled or disabled
 * at runtime based on application properties. It contains nested
 * classes that implement specific conditional behaviors.
 */
public class LogConditions {

    public static class LogTraceCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String datasourceStatus = context.getEnvironment().getProperty("scm.datasource.logTrace.enabled");
            String converterStatus = context.getEnvironment().getProperty("scm.log.logTraceConverter.enabled");
            return "true".equalsIgnoreCase(datasourceStatus) && "true".equalsIgnoreCase(converterStatus);
        }
    }

    public static class TransactionLogTraceCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String datasourceStatus = context.getEnvironment().getProperty("scm.datasource.transactionLog.enabled");
            String converterStatus = context.getEnvironment().getProperty("scm.log.transactionLogConverter.enabled");
            return "true".equalsIgnoreCase(datasourceStatus) && "true".equalsIgnoreCase(converterStatus);
        }
    }




    public static class MessageLogCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            String datasourceStatus = context.getEnvironment().getProperty("scm.datasource.messageLog.enabled");
            String converterStatus = context.getEnvironment().getProperty("scm.log.messageLogConverter.enabled");
            return "true".equalsIgnoreCase(datasourceStatus) && "true".equalsIgnoreCase(converterStatus);
        }
    }


}
