package ir.daneshrefah.scm.observation;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ObservationAttributeTypeMappingTest {
    @Test
    void predefinedTypesMapJavaAndElasticsearchTypes() {
        Map<ObservationAttributeType<?>, ElasticFieldType> expected = Map.of(
                ObservationAttributeTypes.KEYWORD, ElasticFieldType.KEYWORD,
                ObservationAttributeTypes.TEXT, ElasticFieldType.TEXT,
                ObservationAttributeTypes.DATE, ElasticFieldType.DATE,
                ObservationAttributeTypes.INTEGER, ElasticFieldType.INTEGER,
                ObservationAttributeTypes.LONG, ElasticFieldType.LONG,
                ObservationAttributeTypes.DOUBLE, ElasticFieldType.DOUBLE,
                ObservationAttributeTypes.BOOLEAN, ElasticFieldType.BOOLEAN
        );

        expected.forEach((type, elasticType) -> assertEquals(elasticType, type.elasticType()));
        assertEquals(String.class, ObservationAttributeTypes.KEYWORD.javaType());
        assertEquals(Integer.class, ObservationAttributeTypes.INTEGER.javaType());
        assertEquals(Long.class, ObservationAttributeTypes.LONG.javaType());
        assertEquals(Double.class, ObservationAttributeTypes.DOUBLE.javaType());
        assertEquals(Boolean.class, ObservationAttributeTypes.BOOLEAN.javaType());
    }

    @Test
    void objectTypeKeepsRequestedJavaType() {
        ObservationAttributeType<SampleValue> type = ObservationAttributeTypes.object(SampleValue.class);

        assertEquals(SampleValue.class, type.javaType());
        assertEquals(ElasticFieldType.OBJECT, type.elasticType());
    }

    private record SampleValue(String value) {
    }
}
