package ir.daneshrefah.scm.observation.logging;

import ir.daneshrefah.scm.observation.ObservationRecordKind;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class ScmLogMarkers {
    public static final Marker SCM_CONTEXT = MarkerFactory.getMarker("SCM_CONTEXT");
    public static final Marker SCM_EVENT = MarkerFactory.getMarker("SCM_EVENT");
    public static final Marker SCM_EXCEPTION = MarkerFactory.getMarker("SCM_EXCEPTION");
    public static final Marker SCM_CHANGE = MarkerFactory.getMarker("SCM_CHANGE");
    public static final Marker SCM_OBSERVATION_TRACE = MarkerFactory.getMarker("SCM_OBSERVATION_TRACE");
    public static final Marker SCM_OBSERVATION_AUDIT = MarkerFactory.getMarker("SCM_OBSERVATION_AUDIT");

    private ScmLogMarkers() {
    }

    public static Marker recordKindMarker(ObservationRecordKind kind) {
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case CONTEXT -> SCM_CONTEXT;
            case EVENT -> SCM_EVENT;
            case EXCEPTION -> SCM_EXCEPTION;
            case CHANGE -> SCM_CHANGE;
            case PLAIN -> null;
        };
    }
}
