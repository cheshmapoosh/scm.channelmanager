package ir.daneshrefah.scm.core.entity.service.composition;

import ir.daneshrefah.scm.core.converter.ServiceCompositionTypeConverter;
import ir.daneshrefah.scm.core.converter.ServiceImplementationTypeConverter;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.plugin.api.model.service.composition.ServiceCompositionType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Entity
@DiscriminatorValue("3")
public class CompositionServiceEntity extends ServiceEntity {

    @Column(name = "IMPLEMENTATION_COMPOSITION_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceCompositionTypeConverter.class)
    private ServiceCompositionType compositionType;

    public ServiceCompositionType getCompositionType() {
        return compositionType;
    }

    public void setCompositionType(ServiceCompositionType compositionType) {
        this.compositionType = compositionType;
    }

}
