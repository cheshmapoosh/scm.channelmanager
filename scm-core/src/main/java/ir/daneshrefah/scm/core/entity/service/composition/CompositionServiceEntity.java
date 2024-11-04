package ir.daneshrefah.scm.core.entity.service.composition;

import ir.daneshrefah.scm.core.converter.ServiceCompositionTypeConverter;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.common.model.service.ServiceCompositionType;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
public class CompositionServiceEntity extends ServiceEntity {

    @Column(name = "IMPL_COMPOSITION_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceCompositionTypeConverter.class)
    private ServiceCompositionType compositionType;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "SERVICE_ID")
    private List<ResponseEntity> responseList;

}
