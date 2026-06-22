package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmCommonAttributes {
    private ScmCommonAttributes() {
    }

    public static final ObservationAttributeKey<String> TIMESTAMP = ObservationAttributeKey.stringKey("@timestamp", ObservationAttributePresence.ALWAYS_REQUIRED, "Event timestamp");
    public static final ObservationAttributeKey<String> EVENT_STREAM = ObservationAttributeKey.stringKey("event.stream", ObservationAttributePresence.EVENT_OPTIONAL, "Observation stream");
    public static final ObservationAttributeKey<String> EVENT_KIND = ObservationAttributeKey.stringKey("event.kind", ObservationAttributePresence.EVENT_OPTIONAL, "Event kind");
    public static final ObservationAttributeKey<String> EVENT_CATEGORY = ObservationAttributeKey.stringKey("event.category", ObservationAttributePresence.EVENT_REQUIRED, "Event category");
    public static final ObservationAttributeKey<String> EVENT_ACTION = ObservationAttributeKey.stringKey("event.action", ObservationAttributePresence.EVENT_REQUIRED, "Event action");
    public static final ObservationAttributeKey<String> EVENT_OUTCOME = ObservationAttributeKey.stringKey("event.outcome", ObservationAttributePresence.EVENT_REQUIRED, "Event outcome");
    public static final ObservationAttributeKey<String> PLATFORM = ObservationAttributeKey.stringKey("scm.platform", ObservationAttributePresence.EVENT_OPTIONAL, "Source platform");
    public static final ObservationAttributeKey<String> SERVICE_NAME = ObservationAttributeKey.stringKey("service.name", ObservationAttributePresence.EVENT_OPTIONAL, "Emitting service name");
    public static final ObservationAttributeKey<String> DEPLOYMENT_ENVIRONMENT = ObservationAttributeKey.stringKey("deployment.environment", ObservationAttributePresence.CONTEXT_REQUIRED, "Deployment environment");
    public static final ObservationAttributeKey<String> APP_NAME = ObservationAttributeKey.stringKey("scm.app.name", ObservationAttributePresence.EVENT_OPTIONAL, "Application name");
    public static final ObservationAttributeKey<String> APP_PROFILE = ObservationAttributeKey.stringKey("scm.app.profile", ObservationAttributePresence.EVENT_OPTIONAL, "Application profile");
    public static final ObservationAttributeKey<String> APP_LABEL = ObservationAttributeKey.stringKey("scm.app.label", ObservationAttributePresence.EVENT_OPTIONAL, "Application label");
    public static final ObservationAttributeKey<String> GATEWAY_NAME = ObservationAttributeKey.stringKey("scm.gateway.name", ObservationAttributePresence.EVENT_OPTIONAL, "Gateway name");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = ObservationAttributeKey.stringKey("scm.channel.code", ObservationAttributePresence.EVENT_OPTIONAL, "Channel code");
    public static final ObservationAttributeKey<String> CORRELATION_ID = ObservationAttributeKey.stringKey("scm.correlation_id", ObservationAttributePresence.EVENT_OPTIONAL, "Correlation ID");
}
