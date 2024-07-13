package ir.daneshrefah.scm.core.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.service.CustomExternalServiceProviderMetadata;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author dariush abdollahi
 * @version 1.0
 * @since 2024-07-12
 */
@Converter
public class CustomExternalServiceProviderMetadataConverter extends ExternalServiceProviderMetadataConverter<CustomExternalServiceProviderMetadata> {

    @Override
    protected ObjectNode createMetadataObjectNode(CustomExternalServiceProviderMetadata metaData, ObjectNode result) {
        return result;
    }

    @Override
    protected CustomExternalServiceProviderMetadata createMetadataObject(JsonNode jsonMetadata) {
        return new CustomExternalServiceProviderMetadata();
    }
}