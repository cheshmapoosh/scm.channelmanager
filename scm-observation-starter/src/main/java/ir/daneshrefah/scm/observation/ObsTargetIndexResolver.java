package ir.daneshrefah.scm.observation;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ObsTargetIndexResolver {
    private static final DateTimeFormatter INDEX_HOUR = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH");

    public String resolve(ObservationStream stream, ObservationContext context, Instant timestamp) {
        return resolve(stream, context.platform(), context.channelCode(), context.appProfile(), timestamp, context.observationZoneId());
    }

    public String resolve(ObservationStream stream, String platform, String channelCode, String environment, Instant timestamp) {
        return resolve(stream, platform, channelCode, environment, timestamp, ZoneId.of("UTC"));
    }

    public String resolve(ObservationStream stream, String platform, String channelCode, String environment, Instant timestamp, ZoneId zoneId) {
        return stream.value()
                + "-" + normalize(platform)
                + "-" + normalize(channelCode)
                + "-" + normalize(environment)
                + "-" + INDEX_HOUR.withZone(zoneId).format(timestamp);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "default";
        }
        return value.trim();
    }
}
