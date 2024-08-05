package ir.daneshrefah.scm.common.data.audit.util;

import ir.daneshrefah.scm.common.data.audit.model.AuditDetails;
import ir.daneshrefah.scm.common.data.audit.model.MetaData;
import ir.daneshrefah.scm.common.data.audit.model.Parameter;
import jakarta.persistence.metamodel.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class InstanceManager {

    private static final Map<Class<?>, Constructor<?>> CLASS_DEFAULT_CONSTRUCTOR_MAP = new ConcurrentHashMap<>();

    @SneakyThrows
    public Object shallowCopy(AuditDetails auditInfo) {
        MetaData metaData = auditInfo.getMetaData();
        if (Objects.isNull(metaData)) {
            return auditInfo.getData();
        } else {
            Object data = auditInfo.getData();
            List<Parameter> parameters = metaData.getParameters();
            Map<String, Object> dataMap = new HashMap<>();
            for (Parameter parameter : parameters) {
                String name = parameter.getName();
                Attribute.PersistentAttributeType type = parameter.getType();
                if (type.equals(Attribute.PersistentAttributeType.ONE_TO_MANY)
                        || type.equals(Attribute.PersistentAttributeType.MANY_TO_ONE)
                        || type.equals(Attribute.PersistentAttributeType.MANY_TO_MANY)
                        || type.equals(Attribute.PersistentAttributeType.ONE_TO_ONE)) {
                    removeRelationalInstance(parameter, data);
                } else {
                    dataMap.put(name, parameter.getGetterMethod().invoke(data));
                }
            }
            return dataMap;
        }
    }

    private void removeRelationalInstance(Parameter parameter, Object data) {
        try {
            Object refreshInstance = createRefreshInstance(data);
            ReflectionUtils.shallowCopyFieldState(data, refreshInstance);
            parameter.getSetterMethod().invoke(refreshInstance, (Object) null);
        } catch (Exception ignore) {
        }
    }

    @SneakyThrows
    private Object createRefreshInstance(Object instance) {
        return CLASS_DEFAULT_CONSTRUCTOR_MAP.computeIfAbsent(instance.getClass(), aClass -> {
            try {
                return ReflectionUtils.accessibleConstructor(aClass);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(aClass.getName() + " does not have default constructor");
            }
        }).newInstance();
    }

}
