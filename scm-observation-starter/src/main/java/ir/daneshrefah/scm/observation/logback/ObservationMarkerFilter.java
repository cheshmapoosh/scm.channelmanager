package ir.daneshrefah.scm.observation.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

import java.util.List;

public class ObservationMarkerFilter extends Filter<ILoggingEvent> {
    private String marker;
    private boolean inverse;

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    @Override
    public FilterReply decide(ILoggingEvent event) {
        boolean matched = marker != null && !marker.isBlank() && hasMarker(event, marker.trim());
        if (inverse) {
            return matched ? FilterReply.DENY : FilterReply.NEUTRAL;
        }
        return matched ? FilterReply.ACCEPT : FilterReply.DENY;
    }

    private boolean hasMarker(ILoggingEvent event, String markerName) {
        if (event == null) {
            return false;
        }
        List<Marker> markers = event.getMarkerList();
        if (markers == null || markers.isEmpty()) {
            return false;
        }
        for (Marker eventMarker : markers) {
            if (contains(eventMarker, markerName)) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(Marker eventMarker, String markerName) {
        return eventMarker != null && (markerName.equals(eventMarker.getName()) || eventMarker.contains(markerName));
    }
}
