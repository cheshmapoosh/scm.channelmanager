package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.ExternalServiceRequestBodyType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Data
@Entity
public abstract class AbstractExternalServiceEntity<T extends AbstractExternalServiceProviderEntity> extends ServiceEntity {

    @ManyToOne(targetEntity = AbstractExternalServiceProviderEntity.class)
    @JoinColumn(name = "IMPLEMENTATION_SERVICE_PROVIDER_ID")
    private T serviceProvider;
    private ExternalServiceRequestBodyType requestBodyType;

}
