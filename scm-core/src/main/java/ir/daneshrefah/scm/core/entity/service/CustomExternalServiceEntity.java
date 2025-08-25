package ir.daneshrefah.scm.core.entity.service;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("1")
@Deprecated
public class CustomExternalServiceEntity extends AbstractExternalServiceEntity<CustomExternalServiceProviderEntity> {

}
