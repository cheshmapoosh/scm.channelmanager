package ir.daneshrefah.scm.observation;

import java.util.Objects;

public record ObservationAttributeType<T>(
        Class<T> javaType,
        ElasticFieldType elasticType
) {
    public ObservationAttributeType {
        Objects.requireNonNull(javaType, "javaType");
        Objects.requireNonNull(elasticType, "elasticType");
    }
}
