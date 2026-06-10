package ir.daneshrefah.scm.observation.logback;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class ScmObservationMarkers {
    public static final String TRACE_MARKER_NAME = "SCM_OBSERVATION_TRACE";
    public static final String AUDIT_MARKER_NAME = "SCM_OBSERVATION_AUDIT";

    public static final Marker TRACE = MarkerFactory.getMarker(TRACE_MARKER_NAME);
    public static final Marker AUDIT = MarkerFactory.getMarker(AUDIT_MARKER_NAME);

    private ScmObservationMarkers() {
    }
}
