package ir.daneshrefah.scm.common.data.audit.util;


import ir.daneshrefah.scm.common.model.audit.AuditEvent;
import ir.daneshrefah.scm.common.model.audit.MetaData;
import ir.daneshrefah.scm.common.model.audit.Parameter;
import jakarta.persistence.metamodel.Attribute;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
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
    public Object shallowCopy(AuditEvent auditEvent) {
        MetaData metaData = auditEvent.getMetaData();
        if (Objects.isNull(metaData)) {
            return auditEvent.getData();
        } else {
            Object data = auditEvent.getData();
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
                    Method getterMethod = parameter.getGetterMethod();
                    if (Objects.nonNull(getterMethod)) {
                        dataMap.put(name, getterMethod.invoke(data));
                    }
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
