package ir.daneshrefah.scm.observation;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class ObservationAttributeKey<T> {
    public static final String ELASTIC_DATE = "date";
    public static final String ELASTIC_KEYWORD = "keyword";
    public static final String ELASTIC_TEXT = "text";
    public static final String ELASTIC_INTEGER = "integer";

    private final String name;
    private final Class<T> type;
    private final String elasticType;
    private final String owner;
    private final Set<ObservationStream> streams;
    private final ObservationAttributePresence presence;
    private final ObservationAttributeSensitivity sensitivity;
    private final int visiblePrefixLength;
    private final int visibleSuffixLength;
    private final String description;

    private ObservationAttributeKey(
            String name,
            Class<T> type,
            String elasticType,
            String owner,
            Set<ObservationStream> streams,
            ObservationAttributePresence presence,
            ObservationAttributeSensitivity sensitivity,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description
    ) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
        this.elasticType = Objects.requireNonNull(elasticType, "elasticType");
        this.owner = owner == null || owner.isBlank() ? "common" : owner.trim();
        this.streams = Collections.unmodifiableSet(streams == null || streams.isEmpty()
                ? EnumSet.of(ObservationStream.LOG)
                : EnumSet.copyOf(streams));
        this.presence = presence == null ? ObservationAttributePresence.EVENT_OPTIONAL : presence;
        this.sensitivity = sensitivity == null ? ObservationAttributeSensitivity.RAW : sensitivity;
        this.visiblePrefixLength = Math.max(0, visiblePrefixLength);
        this.visibleSuffixLength = Math.max(0, visibleSuffixLength);
        this.description = description == null ? "" : description;
    }

    public static ObservationAttributeKey<String> logString(
            String name,
            String elasticType,
            String owner,
            ObservationAttributePresence presence,
            ObservationAttributeSensitivity sensitivity,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description
    ) {
        return new ObservationAttributeKey<>(
                name,
                String.class,
                elasticType,
                owner,
                EnumSet.of(ObservationStream.LOG),
                presence,
                sensitivity,
                visiblePrefixLength,
                visibleSuffixLength,
                description
        );
    }

    public static ObservationAttributeKey<Integer> logInteger(
            String name,
            String owner,
            ObservationAttributePresence presence,
            ObservationAttributeSensitivity sensitivity,
            int visiblePrefixLength,
            int visibleSuffixLength,
            String description
    ) {
        return new ObservationAttributeKey<>(
                name,
                Integer.class,
                ELASTIC_INTEGER,
                owner,
                EnumSet.of(ObservationStream.LOG),
                presence,
                sensitivity,
                visiblePrefixLength,
                visibleSuffixLength,
                description
        );
    }

    public static ObservationAttributeKey<String> stringKey(
            String name,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        return typedKey(name, String.class, ELASTIC_KEYWORD, presence, description, streams);
    }

    public static ObservationAttributeKey<String> dateKey(
            String name,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        return typedKey(name, String.class, ELASTIC_DATE, presence, description, streams);
    }

    public static ObservationAttributeKey<Boolean> booleanKey(
            String name,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        return typedKey(name, Boolean.class, ELASTIC_KEYWORD, presence, description, streams);
    }

    public static ObservationAttributeKey<Integer> integerKey(
            String name,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        return typedKey(name, Integer.class, ELASTIC_INTEGER, presence, description, streams);
    }

    public static ObservationAttributeKey<Long> longKey(
            String name,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        return typedKey(name, Long.class, ELASTIC_INTEGER, presence, description, streams);
    }

    public static ObservationAttributeKey<Double> doubleKey(
            String name,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        return typedKey(name, Double.class, ELASTIC_INTEGER, presence, description, streams);
    }

    public static ObservationAttributeKey<Object> objectKey(
            String name,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        return typedKey(name, Object.class, ELASTIC_KEYWORD, presence, description, streams);
    }

    @SafeVarargs
    public static <T> ObservationAttributeKey<T> key(
            String name,
            Class<T> type,
            String elasticType,
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
                elasticType,
                owner,
                streamSet,
                presence,
                sensitivity,
                visiblePrefixLength,
                visibleSuffixLength,
                description
        );
    }

    private static <T> ObservationAttributeKey<T> typedKey(
            String name,
            Class<T> type,
            String elasticType,
            ObservationAttributePresence presence,
            String description,
            ObservationStream... streams
    ) {
        Set<ObservationStream> streamSet = streams == null || streams.length == 0
                ? EnumSet.of(ObservationStream.LOG)
                : EnumSet.copyOf(Arrays.asList(streams));
        return new ObservationAttributeKey<>(
                name,
                type,
                elasticType,
                "starter",
                streamSet,
                presence,
                ObservationAttributeSensitivity.RAW,
                0,
                0,
                description
        );
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    public String elasticType() {
        return elasticType;
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
                && elasticType.equals(other.elasticType)
                && owner.equals(other.owner)
                && streams.equals(other.streams)
                && presence == other.presence
                && sensitivity == other.sensitivity
                && visiblePrefixLength == other.visiblePrefixLength
                && visibleSuffixLength == other.visibleSuffixLength;
    }
}
