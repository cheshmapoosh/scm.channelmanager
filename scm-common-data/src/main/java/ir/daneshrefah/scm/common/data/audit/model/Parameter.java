package ir.daneshrefah.scm.common.data.audit.model;

import jakarta.persistence.metamodel.Attribute;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.lang.reflect.Method;

@Setter
@Getter
@Accessors(chain = true)
public class Parameter {
    private String name;
    private Attribute.PersistentAttributeType type;
    private Method getterMethod;
    private Method setterMethod;
}
