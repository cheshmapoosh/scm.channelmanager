package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.core.converter.ExternalServiceRequestBodyTypeConverter;
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
@Setter
@Getter
@Entity
public abstract class AbstractExternalServiceEntity<T extends AbstractExternalServiceProviderEntity> extends ServiceEntity {

    @ManyToOne(targetEntity = AbstractExternalServiceProviderEntity.class)
    @JoinColumn(name = "IMPL_SERVICE_PROVIDER_ID")
    private T serviceProvider;
    @Convert(converter = ExternalServiceRequestBodyTypeConverter.class)
    private ExternalServiceBodyType requestBodyType;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "SERVICE_ID")
    private List<ResponseEntity> responseList;

}
