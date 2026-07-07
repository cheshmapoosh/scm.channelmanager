package ir.daneshrefah.scm.observation.autoconfigure;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import ir.daneshrefah.scm.observation.ObsTargetIndexResolver;
import ir.daneshrefah.scm.observation.ObservationAttributeContributor;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistry;
import ir.daneshrefah.scm.observation.ObservationAttributeRegistryHolder;
import ir.daneshrefah.scm.observation.ObservationContext;
import ir.daneshrefah.scm.observation.ObservationContextHolder;
import ir.daneshrefah.scm.observation.ObservationDocumentFactory;
import ir.daneshrefah.scm.observation.ObservationDocumentSerializer;
import ir.daneshrefah.scm.observation.ObservationEventDispatcher;
import ir.daneshrefah.scm.observation.ObservationEventSink;
import ir.daneshrefah.scm.observation.ObservationProperties;
import ir.daneshrefah.scm.observation.ObservationRecordValidator;
import ir.daneshrefah.scm.observation.ObservationSanitizer;
import ir.daneshrefah.scm.observation.ScmObservation;
import ir.daneshrefah.scm.observation.SecretScrubbingObservationSanitizer;
import ir.daneshrefah.scm.observation.element.ScmElementHealthEngine;
import ir.daneshrefah.scm.observation.element.ScmElementRiskEngine;
import ir.daneshrefah.scm.observation.gateway.GatewayObservationLifecycle;
import ir.daneshrefah.scm.observation.logback.DefaultLogbackObservationEventPublisher;
import ir.daneshrefah.scm.observation.logback.LogbackAuditObservationEventSink;
import ir.daneshrefah.scm.observation.logback.LogbackObservationEventPublisher;
import ir.daneshrefah.scm.observation.logback.LogbackTraceObservationEventSink;
import ir.daneshrefah.scm.observation.metrics.MetricObservationSink;
import ir.daneshrefah.scm.observation.metrics.MicrometerMetricObservationSink;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import ir.daneshrefah.scm.observation.policy.ResolvedObservationSignalPolicy;
import ir.daneshrefah.scm.observation.trace.NoopTraceObservationSink;
import ir.daneshrefah.scm.observation.trace.StructuredTraceObservationSink;
import ir.daneshrefah.scm.observation.trace.TraceObservationSink;
import ir.daneshrefah.scm.observation.web.HttpServerObservationFilter;
import ir.daneshrefah.scm.observation.web.ObservationMdcFilter;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
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
    public ObservationConfigurationValidator observationConfigurationValidator(
            ObservationProperties properties,
            ObservationSignalPolicy signalPolicy,
            ObjectProvider<LogbackObservationEventPublisher> publisherProvider
    ) {
        return new ObservationConfigurationValidator(properties, signalPolicy, publisherProvider);
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

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Filter.class)
    static class LogMdcConfiguration {
        @Bean
        @ConditionalOnLogEnabled
        @ConditionalOnMissingBean(name = "observationMdcFilterRegistration")
        public FilterRegistrationBean<ObservationMdcFilter> observationMdcFilterRegistration(
                ObservationSignalPolicy signalPolicy,
                ObservationContext context
        ) {
            FilterRegistrationBean<ObservationMdcFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new ObservationMdcFilter(signalPolicy, context));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
            registration.addUrlPatterns("/*");
            return registration;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(Filter.class)
    static class HttpServerTraceConfiguration {
        @Bean
        @ConditionalOnTraceEnabled
        @ConditionalOnMissingBean(name = "httpServerObservationFilterRegistration")
        public FilterRegistrationBean<HttpServerObservationFilter> httpServerObservationFilterRegistration(
                ScmObservation observation,
                ObservationSignalPolicy signalPolicy,
                ObservationProperties properties
        ) {
            ObservationProperties.ServerProperties server = properties.getHttp().getServer();
            FilterRegistrationBean<HttpServerObservationFilter> registration = new FilterRegistrationBean<>();
            registration.setFilter(new HttpServerObservationFilter(
                    observation,
                    signalPolicy,
                    server.getSpanName()
            ));
            registration.setEnabled(server.isEnabled() && !"channel-only".equalsIgnoreCase(server.getMode()));
            registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
            registration.addUrlPatterns("/*");
            return registration;
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

    @Conditional(LogSignalCondition.class)
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @interface ConditionalOnLogEnabled {
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
