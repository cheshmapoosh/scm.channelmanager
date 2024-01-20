package ir.daneshrefah.scm.core.entity.service;

import jakarta.persistence.DiscriminatorValue;
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
@Getter
@Setter
@Entity
@DiscriminatorValue("1")
public class ExternalServiceEntity extends ServiceEntity {

    @ManyToOne
    @JoinColumn(name = "IMPLEMENTATION_SERVICE_PROVIDER_ID")
    private ExternalServiceProviderEntity serviceProvider;

}
