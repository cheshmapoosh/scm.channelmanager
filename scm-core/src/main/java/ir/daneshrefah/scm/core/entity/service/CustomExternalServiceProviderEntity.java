package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProviderMetadata;
import ir.daneshrefah.scm.common.model.service.CustomExternalServiceProviderMetadata;
import ir.daneshrefah.scm.core.converter.CustomExternalServiceProviderMetadataConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-12
 */
@Data
@Entity
@DiscriminatorValue("20") // ServiceProviderProtocol.CUSTOM
public class CustomExternalServiceProviderEntity extends AbstractExternalServiceProviderEntity {

    @Convert(converter = CustomExternalServiceProviderMetadataConverter.class)
    @Column(name = "SRV_PROVIDER_METADATA")
    private CustomExternalServiceProviderMetadata metadata;

    @Override
    public void setMetadata(AbstractExternalServiceProviderMetadata metadata) {
        this.metadata = (CustomExternalServiceProviderMetadata) metadata;
    }
}
