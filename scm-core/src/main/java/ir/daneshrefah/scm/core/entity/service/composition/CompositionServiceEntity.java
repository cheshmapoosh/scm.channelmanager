package ir.daneshrefah.scm.core.entity.service.composition;

import ir.daneshrefah.scm.common.model.service.ServiceCompositionType;
import ir.daneshrefah.scm.core.converter.ServiceCompositionTypeConverter;
import ir.daneshrefah.scm.core.entity.service.ScmServiceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Entity
@DiscriminatorValue("3")
@Getter
@Setter
@Deprecated
public class CompositionServiceEntity extends ScmServiceEntity {

    @Column(name = "IMPL_COMPOSITION_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceCompositionTypeConverter.class)
    private ServiceCompositionType compositionType;

}
