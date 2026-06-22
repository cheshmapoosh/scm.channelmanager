package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationAttributeSensitivity;
import ir.daneshrefah.scm.observation.ObservationStream;

import java.util.List;

public final class ScmObservationDocumentAttributes {
    private static final String OWNER = "common";

    public static final ObservationAttributeKey<String> TIMESTAMP = key(
            "@timestamp",
            ObservationAttributeKey.ELASTIC_DATE,
            ObservationAttributePresence.ALWAYS_REQUIRED,
            "Observation event timestamp."
    );
    public static final ObservationAttributeKey<String> MESSAGE = key(
            "message",
            ObservationAttributeKey.ELASTIC_TEXT,
            ObservationAttributePresence.ALWAYS_REQUIRED,
            "Rendered observation message."
    );
    public static final ObservationAttributeKey<String> CORRELATION_ID = key(
            "correlation.id",
            ObservationAttributeKey.ELASTIC_KEYWORD,
            ObservationAttributePresence.ALWAYS_REQUIRED,
            "Correlation id for the active CorrelationType context."
    );
    public static final ObservationAttributeKey<String> CORRELATION_TYPE = key(
            "correlation.type",
            ObservationAttributeKey.ELASTIC_KEYWORD,
            ObservationAttributePresence.ALWAYS_REQUIRED,
            "Correlation type from the CorrelationType enum."
    );
    public static final ObservationAttributeKey<String> EVENT_CATEGORY = key(
            "event.category",
            ObservationAttributeKey.ELASTIC_KEYWORD,
            ObservationAttributePresence.EVENT_REQUIRED,
            "SCM event category."
    );
    public static final ObservationAttributeKey<String> EVENT_ACTION = key(
            "event.action",
            ObservationAttributeKey.ELASTIC_KEYWORD,
            ObservationAttributePresence.EVENT_REQUIRED,
            "SCM event action."
    );
    public static final ObservationAttributeKey<String> EVENT_OUTCOME = key(
            "event.outcome",
            ObservationAttributeKey.ELASTIC_KEYWORD,
            ObservationAttributePresence.EVENT_REQUIRED,
            "SCM event outcome: success, failure, unknown, or skipped."
    );

    private ScmObservationDocumentAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                TIMESTAMP,
                MESSAGE,
                CORRELATION_ID,
                CORRELATION_TYPE,
                EVENT_CATEGORY,
                EVENT_ACTION,
                EVENT_OUTCOME
        );
    }

    private static ObservationAttributeKey<String> key(
            String name,
            String elasticType,
            ObservationAttributePresence presence,
            String description
    ) {
        return ObservationAttributeKey.key(
                name,
                String.class,
                elasticType,
                OWNER,
                presence,
                ObservationAttributeSensitivity.RAW,
                0,
                0,
                description,
                ObservationStream.TRACE,
                ObservationStream.AUDIT
        );
    }
}
