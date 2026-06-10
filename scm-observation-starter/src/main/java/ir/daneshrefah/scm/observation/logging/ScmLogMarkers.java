package ir.daneshrefah.scm.observation.logging;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class ScmLogMarkers {
    public static final Marker SCM_INIT = MarkerFactory.getMarker("SCM_INIT");
    public static final Marker SCM_OBSERVATION_TRACE = MarkerFactory.getMarker("SCM_OBSERVATION_TRACE");
    public static final Marker SCM_OBSERVATION_AUDIT = MarkerFactory.getMarker("SCM_OBSERVATION_AUDIT");

    private ScmLogMarkers() {
    }
}
