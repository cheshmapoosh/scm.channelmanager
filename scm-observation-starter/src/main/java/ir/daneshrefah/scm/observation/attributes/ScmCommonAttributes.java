package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmCommonAttributes {
    private ScmCommonAttributes() {
    }

    public static final ObservationAttributeKey<String> TIMESTAMP = ObservationAttributeKey.stringKey("@timestamp", true, "Event timestamp");
    public static final ObservationAttributeKey<String> EVENT_STREAM = ObservationAttributeKey.stringKey("event.stream", true, "Observation stream");
    public static final ObservationAttributeKey<String> EVENT_KIND = ObservationAttributeKey.stringKey("event.kind", true, "Event kind");
    public static final ObservationAttributeKey<String> EVENT_CATEGORY = ObservationAttributeKey.stringKey("event.category", true, "Event category");
    public static final ObservationAttributeKey<String> EVENT_ACTION = ObservationAttributeKey.stringKey("event.action", true, "Event action");
    public static final ObservationAttributeKey<String> EVENT_OUTCOME = ObservationAttributeKey.stringKey("event.outcome", true, "Event outcome");
    public static final ObservationAttributeKey<String> PLATFORM = ObservationAttributeKey.stringKey("scm.platform", true, "Source platform");
    public static final ObservationAttributeKey<String> SERVICE_NAME = ObservationAttributeKey.stringKey("service.name", true, "Emitting service name");
    public static final ObservationAttributeKey<String> DEPLOYMENT_ENVIRONMENT = ObservationAttributeKey.stringKey("deployment.environment", true, "Deployment environment");
    public static final ObservationAttributeKey<String> APP_NAME = ObservationAttributeKey.stringKey("scm.app.name", true, "Application name");
    public static final ObservationAttributeKey<String> APP_PROFILE = ObservationAttributeKey.stringKey("scm.app.profile", true, "Application profile");
    public static final ObservationAttributeKey<String> APP_LABEL = ObservationAttributeKey.stringKey("scm.app.label", true, "Application label");
    public static final ObservationAttributeKey<String> GATEWAY_NAME = ObservationAttributeKey.stringKey("scm.gateway.name", true, "Gateway name");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = ObservationAttributeKey.stringKey("scm.channel.code", true, "Channel code");
    public static final ObservationAttributeKey<String> CORRELATION_ID = ObservationAttributeKey.stringKey("scm.correlation_id", true, "Correlation ID");
}
