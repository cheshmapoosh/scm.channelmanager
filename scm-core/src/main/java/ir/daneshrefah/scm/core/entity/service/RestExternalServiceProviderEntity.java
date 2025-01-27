package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProviderMetadata;
import ir.daneshrefah.scm.common.model.service.RestExternalServiceProviderMetadata;
import ir.daneshrefah.scm.core.converter.RestExternalServiceProviderMetadataConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-12
 */
@Getter
@Setter
@Entity
@DiscriminatorValue("1") // ServiceProviderProtocol.REST
public class RestExternalServiceProviderEntity extends AbstractExternalServiceProviderEntity {

}
