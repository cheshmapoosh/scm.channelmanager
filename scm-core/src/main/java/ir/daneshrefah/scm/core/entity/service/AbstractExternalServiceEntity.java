package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.core.converter.ExternalServiceRequestBodyTypeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

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
@Deprecated
public abstract class AbstractExternalServiceEntity<T extends AbstractExternalServiceProviderEntity> extends ScmServiceEntity {

    @ManyToOne(targetEntity = AbstractExternalServiceProviderEntity.class)
    @JoinColumn(name = "IMPL_SERVICE_PROVIDER_ID")
    private T serviceProvider;
    @Convert(converter = ExternalServiceRequestBodyTypeConverter.class)
    private ExternalServiceBodyType requestBodyType;

}
