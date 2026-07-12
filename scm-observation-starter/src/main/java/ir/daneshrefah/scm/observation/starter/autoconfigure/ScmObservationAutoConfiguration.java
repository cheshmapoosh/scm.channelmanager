package ir.daneshrefah.scm.observation.starter.autoconfigure;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import ir.daneshrefah.scm.observation.starter.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.starter.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.starter.ObservationContext;
import ir.daneshrefah.scm.observation.starter.ObservationContextHolder;
import ir.daneshrefah.scm.observation.starter.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.starter.ObservationDocumentSerializer;
import ir.daneshrefah.scm.observation.starter.ObservationEventDispatcher;
import ir.daneshrefah.scm.observation.starter.ObservationEventSink;
import ir.daneshrefah.scm.observation.starter.ObservationProperties;
import ir.daneshrefah.scm.observation.starter.ObservationRecordValidator;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.ScmObservation;
import ir.daneshrefah.scm.observation.starter.SecretScrubbingObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.element.ScmElementHealthEngine;
import ir.daneshrefah.scm.observation.starter.element.ScmElementRiskEngine;
import ir.daneshrefah.scm.observation.starter.gateway.GatewayObservationLifecycle;
import ir.daneshrefah.scm.observation.starter.logback.DefaultLogbackObservationEventPublisher;
import ir.daneshrefah.scm.observation.starter.logback.LogbackAuditObservationEventSink;
import ir.daneshrefah.scm.observation.starter.logback.LogbackObservationEventPublisher;
import ir.daneshrefah.scm.observation.starter.logback.LogbackTraceObservationEventSink;
import ir.daneshrefah.scm.observation.starter.metrics.MetricObservationSink;
import ir.daneshrefah.scm.observation.starter.metrics.MicrometerMetricObservationSink;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignalPolicy;
import ir.daneshrefah.scm.observation.starter.policy.ResolvedObservationSignalPolicy;
import ir.daneshrefah.scm.observation.starter.scheduled.ScheduledObservationLifecycle;
import ir.daneshrefah.scm.observation.starter.trace.NoopTraceObservationSink;
import ir.daneshrefah.scm.observation.starter.trace.ScmSpanDataMapper;
import ir.daneshrefah.scm.observation.starter.trace.StructuredTraceObservationSink;
import ir.daneshrefah.scm.observation.starter.trace.TraceObservationSink;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@AutoConfiguration
@EnableConfigurationProperties(ObservationProperties.class)
public class ScmObservationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObservationContext observationContext(ObservationProperties properties, Environment environment) {
        ObservationContext context = ObservationContext.from(properties, environment);
        ObservationContextHolder.set(context);
        return context;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationSignalPolicy observationSignalPolicy(ObservationProperties properties) {
        return new ResolvedObservationSignalPolicy(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObsTargetIndexResolver obsTargetIndexResolver() {
        return new ObsTargetIndexResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationAttributeRegistry observationAttributeRegistry(
            ObjectProvider<ObservationAttributeContributor> contributors
    ) {
        ObservationAttributeRegistry registry = new ObservationAttributeRegistry(contributors.orderedStream().toList());
        ObservationAttributeRegistryHolder.set(registry);
        return registry;
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationSanitizer observationSanitizer() {
        return new SecretScrubbingObservationSanitizer();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationDocumentFactory observationDocumentFactory(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer
    ) {
        ObservationContextHolder.set(context);
        ObservationAttributeRegistryHolder.set(registry);
        return new ObservationDocumentFactory(context, registry, sanitizer, targetIndexResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationRecordValidator observationRecordValidator(ObservationAttributeRegistry registry) {
        return new ObservationRecordValidator(registry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmElementRiskEngine scmElementRiskEngine() {
        return new ScmElementRiskEngine();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmElementHealthEngine scmElementHealthEngine(ScmElementRiskEngine riskEngine) {
        return new ScmElementHealthEngine(riskEngine);
    }

    @Bean
    @ConditionalOnMissingBean
    public Clock observationClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper observationObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationDocumentSerializer observationDocumentSerializer(ObjectMapper objectMapper) {
        return new ObservationDocumentSerializer(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationEventDispatcher observationEventDispatcher(
            ObservationSignalPolicy signalPolicy,
            ObjectProvider<ObservationEventSink> eventSinks
    ) {
        return new ObservationEventDispatcher(signalPolicy, eventSinks.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmObservation scmObservation(
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationSignalPolicy signalPolicy,
            ObservationEventDispatcher eventDispatcher,
            ObjectProvider<MetricObservationSink> metricSinkProvider,
            ObjectProvider<TraceObservationSink> traceSinkProvider,
            ObservationSanitizer sanitizer,
            ObservationDocumentFactory documentFactory,
            ObservationRecordValidator recordValidator,
            Clock observationClock
    ) {
        return new ScmObservation(
                context,
                targetIndexResolver,
                signalPolicy,
                eventDispatcher,
                metricSinkProvider.getIfAvailable(),
                traceSinkProvider.getIfAvailable(NoopTraceObservationSink::new),
                sanitizer,
                documentFactory,
                recordValidator,
                observationClock
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public GatewayObservationLifecycle gatewayObservationLifecycle(
            ScmObservation observation,
            ObservationContext context
    ) {
        return new GatewayObservationLifecycle(observation, context);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledObservationLifecycle scheduledObservationLifecycle(ScmObservation observation) {
        return new ScheduledObservationLifecycle(observation);
    }

    @Bean
    @ConditionalOnMissingBean
    public ObservationConfigurationValidator observationConfigurationValidator(
            ObservationProperties properties,
            ObservationSignalPolicy signalPolicy,
            ObjectProvider<LogbackObservationEventPublisher> publisherProvider,
            Environment environment
    ) {
        return new ObservationConfigurationValidator(properties, signalPolicy, publisherProvider, environment);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(LoggerContext.class)
    @Conditional(EventSignalCondition.class)
    static class LogbackEventConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public LogbackObservationEventPublisher logbackObservationEventPublisher() {
            return new DefaultLogbackObservationEventPublisher();
        }

        @Bean
        @ConditionalOnTraceEnabled
        @ConditionalOnMissingBean(name = "logbackTraceObservationEventSink")
        public ObservationEventSink logbackTraceObservationEventSink(
                ObservationDocumentSerializer serializer,
                LogbackObservationEventPublisher publisher
        ) {
            return new LogbackTraceObservationEventSink(serializer, publisher);
        }

        @Bean
        @ConditionalOnAuditEnabled
        @ConditionalOnMissingBean(name = "logbackAuditObservationEventSink")
        public ObservationEventSink logbackAuditObservationEventSink(
                ObservationDocumentSerializer serializer,
                LogbackObservationEventPublisher publisher
        ) {
            return new LogbackAuditObservationEventSink(serializer, publisher);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class TraceConfiguration {
        @Bean
        @ConditionalOnTraceEnabled
        @ConditionalOnMissingBean
        public TraceObservationSink traceObservationSink(
                ObservationEventDispatcher eventDispatcher,
                ObservationDocumentFactory documentFactory,
                ObservationRecordValidator recordValidator,
                Clock observationClock
        ) {
            return new StructuredTraceObservationSink(
                    eventDispatcher,
                    documentFactory,
                    recordValidator,
                    observationClock
            );
        }

        @Bean
        @ConditionalOnTraceEnabled
        @ConditionalOnMissingBean
        public ScmSpanDataMapper scmSpanDataMapper(
                ObservationContext context,
                ObsTargetIndexResolver targetIndexResolver,
                ObservationSanitizer sanitizer,
                ObservationAttributeRegistry attributeRegistry
        ) {
            return new ScmSpanDataMapper(
                    context,
                    targetIndexResolver,
                    sanitizer,
                    attributeRegistry
            );
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class MetricConfiguration {
        @Bean
        @ConditionalOnMetricEnabled
        @ConditionalOnBean(MeterRegistry.class)
        @ConditionalOnMissingBean
        public MetricObservationSink metricObservationSink(MeterRegistry meterRegistry) {
            return new MicrometerMetricObservationSink(meterRegistry);
        }
    }

    @Conditional(TraceSignalCondition.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface ConditionalOnTraceEnabled {
    }

    @Conditional(AuditSignalCondition.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface ConditionalOnAuditEnabled {
    }

    @Conditional(MetricSignalCondition.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface ConditionalOnMetricEnabled {
    }

    static final class EventSignalCondition extends ObservationSignalConditionSupport {
        @Override
        protected String[] signalProperties() {
            return new String[]{
                    "scm.observation.trace.enabled",
                    "scm.observation.audit.enabled"
            };
        }
    }
}
