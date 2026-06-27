package ir.daneshrefah.scm.observation;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class ObservationAttributeKey<T> {
    private final String name;
    private final ObservationAttributeType<T> type;
    private final String owner;
    private final Set<ObservationStream> streams;
    private final ObservationAttributePresence presence;
    private final ObservationAttributeSensitivity sensitivity;
    private final int visiblePrefixLength;
    private final int visibleSuffixLength;
    private final String description;

    private ObservationAttributeKey(
            String name,
            ObservationAttributeType<T> type,
            String owner,
            Set<ObservationStream> streams,
            ObservationAttributePresence presence,
            ObservationAttributeSensitivity sensitivity,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description
    ) {
        this.name = requireName(name);
        this.type = Objects.requireNonNull(type, "type");
        this.owner = owner == null || owner.isBlank() ? "common" : owner.trim();
        this.streams = Collections.unmodifiableSet(streams == null || streams.isEmpty()
                ? EnumSet.of(ObservationStream.LOG)
                : EnumSet.copyOf(streams));
        this.presence = presence == null ? ObservationAttributePresence.EVENT_OPTIONAL : presence;
        this.sensitivity = sensitivity == null ? ObservationAttributeSensitivity.RAW : sensitivity;
        this.visiblePrefixLength = Math.max(0, visiblePrefixLength);
        this.visibleSuffixLength = Math.max(0, visibleSuffixLength);
        this.description = description == null ? "" : description.trim();
    }

    @SafeVarargs
    public static <T> ObservationAttributeKey<T> key(
            String name,
            ObservationAttributeType<T> type,
            String owner,
            ObservationAttributePresence presence,
            ObservationAttributeSensitivity sensitivity,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description,
            ObservationStream... streams
    ) {
        Set<ObservationStream> streamSet = streams == null || streams.length == 0
                ? EnumSet.of(ObservationStream.LOG)
                : EnumSet.copyOf(Arrays.asList(streams));
        return new ObservationAttributeKey<>(
                name,
                type,
                owner,
                streamSet,
                presence,
                sensitivity,
                visiblePrefixLength,
                visibleSuffixLength,
                description
        );
    }

    public String name() {
        return name;
    }

    public ObservationAttributeType<T> type() {
        return type;
    }

    public String owner() {
        return owner;
    }

    public Set<ObservationStream> streams() {
        return streams;
    }

    public ObservationAttributePresence presence() {
        return presence;
    }

    public ObservationAttributeSensitivity sensitivity() {
        return sensitivity;
    }

    public int visiblePrefixLength() {
        return visiblePrefixLength;
    }

    public int visibleSuffixLength() {
        return visibleSuffixLength;
    }

    public String description() {
        return description;
    }

    public boolean compatibleWith(ObservationAttributeKey<?> other) {
        return other != null
                && name.equals(other.name)
                && type.equals(other.type)
                && owner.equals(other.owner)
                && streams.equals(other.streams)
                && presence == other.presence
                && sensitivity == other.sensitivity
                && visiblePrefixLength == other.visiblePrefixLength
                && visibleSuffixLength == other.visibleSuffixLength;
    }

    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Observation attribute name is required.");
        }
        return name.trim();
    }
}
