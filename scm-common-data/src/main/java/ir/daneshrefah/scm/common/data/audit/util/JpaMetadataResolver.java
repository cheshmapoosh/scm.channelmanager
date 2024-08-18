package ir.daneshrefah.scm.common.data.audit.util;


import ir.daneshrefah.scm.common.model.audit.MetaData;
import ir.daneshrefah.scm.common.model.audit.Parameter;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JpaMetadataResolver {

    private static final Map<Class<?>, MetaData> CLASS_META_DATA_MAP = new HashMap<>();

    private final List<EntityManager> entityManagers;

    @PostConstruct
    private void init() {
        entityManagers.forEach(entityManager -> {
            entityManager.getMetamodel().getEntities().forEach(entityType -> {
                Class<?> javaType = entityType.getJavaType();
                MetaData metaData = new MetaData().setParameters(entityType.getAttributes().stream().map(attribute -> {
                    Parameter parameter = new Parameter();
                    parameter.setName(attribute.getName());
                    parameter.setType(attribute.getPersistentAttributeType());
                    String name = attribute.getName().replace(attribute.getName().substring(0, 1), attribute.getName().substring(0, 1).toUpperCase());
                    parameter.setGetterMethod(ReflectionUtils.findMethod(javaType, "get" + name));
                    parameter.setSetterMethod(ReflectionUtils.findMethod(javaType, "set" + name, attribute.getJavaType()));
                    return parameter;
                }).collect(Collectors.toList()));
                metaData.setIdFieldName(findIdFiledName(javaType));
                CLASS_META_DATA_MAP.put(javaType, metaData);
            });
        });
        log.info(">>> {} entity metadata has been cached.", CLASS_META_DATA_MAP.size());
    }

    private String findIdFiledName(Class<?> javaType) {
        return Arrays.stream(javaType.getDeclaredFields()).filter(field -> Objects.nonNull(field.getDeclaredAnnotation(jakarta.persistence.Id.class))).map(Field::getName).findFirst().orElse(null);
    }

    public Optional<MetaData> getMetaData(Class<?> entityClass) {
        return Optional.ofNullable(CLASS_META_DATA_MAP.getOrDefault(entityClass, null));
    }

}
