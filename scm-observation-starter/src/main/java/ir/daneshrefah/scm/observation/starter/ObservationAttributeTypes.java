package ir.daneshrefah.scm.observation.starter;

import java.util.List;
import java.util.Objects;

public final class ObservationAttributeTypes {
    public static final ObservationAttributeType<String> KEYWORD =
            new ObservationAttributeType<>(String.class, ElasticFieldType.KEYWORD);
    public static final ObservationAttributeType<List<String>> KEYWORD_COLLECTION =
            new ObservationAttributeType<>(listClass(), ElasticFieldType.KEYWORD);
    public static final ObservationAttributeType<String> TEXT =
            new ObservationAttributeType<>(String.class, ElasticFieldType.TEXT);
    public static final ObservationAttributeType<String> DATE =
            new ObservationAttributeType<>(String.class, ElasticFieldType.DATE);
    public static final ObservationAttributeType<Integer> INTEGER =
            new ObservationAttributeType<>(Integer.class, ElasticFieldType.INTEGER);
    public static final ObservationAttributeType<Long> LONG =
            new ObservationAttributeType<>(Long.class, ElasticFieldType.LONG);
    public static final ObservationAttributeType<Double> DOUBLE =
            new ObservationAttributeType<>(Double.class, ElasticFieldType.DOUBLE);
    public static final ObservationAttributeType<Boolean> BOOLEAN =
            new ObservationAttributeType<>(Boolean.class, ElasticFieldType.BOOLEAN);

    private ObservationAttributeTypes() {
    }

    public static <T> ObservationAttributeType<T> object(Class<T> javaType) {
        return new ObservationAttributeType<>(Objects.requireNonNull(javaType, "javaType"), ElasticFieldType.OBJECT);
    }

    @SuppressWarnings("unchecked")
    private static Class<List<String>> listClass() {
        return (Class<List<String>>) (Class<?>) List.class;
    }
}
