package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmLogAttributes {
    private ScmLogAttributes() {
    }

    public static final ObservationAttributeKey<String> LEVEL = ObservationAttributeKey.stringKey("log.level", true, "Log level");
    public static final ObservationAttributeKey<String> LOGGER_NAME = ObservationAttributeKey.stringKey("logger.name", true, "Logger name");
    public static final ObservationAttributeKey<String> THREAD_NAME = ObservationAttributeKey.stringKey("thread.name", true, "Thread name");
    public static final ObservationAttributeKey<String> MESSAGE = ObservationAttributeKey.stringKey("message", true, "Log message");
}
