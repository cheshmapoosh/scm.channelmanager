package ir.daneshrefah.scm.common.model.audit;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private Method getterMethod;
    @JsonIgnore
    private Method setterMethod;
}
