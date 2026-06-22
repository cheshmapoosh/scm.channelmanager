package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmLogAttributes {
    private ScmLogAttributes() {
    }

    public static final ObservationAttributeKey<String> LEVEL = ObservationAttributeKey.stringKey("log.level", ObservationAttributePresence.ALWAYS_REQUIRED, "Log level");
    public static final ObservationAttributeKey<String> LOGGER_NAME = ObservationAttributeKey.stringKey("logger.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Logger name");
    public static final ObservationAttributeKey<String> THREAD_NAME = ObservationAttributeKey.stringKey("thread.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Thread name");
    public static final ObservationAttributeKey<String> MESSAGE = ObservationAttributeKey.stringKey("message", ObservationAttributePresence.ALWAYS_REQUIRED, "Log message");
}
