package ir.daneshrefah.scm.core.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.service.RestExternalServiceProviderMetadata;
import jakarta.persistence.Converter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-12
 */
@Converter
public class RestExternalServiceProviderMetadataConverter extends ExternalServiceProviderMetadataConverter<RestExternalServiceProviderMetadata> {


    @Override
    protected ObjectNode createMetadataObjectNode(RestExternalServiceProviderMetadata metaData, ObjectNode result) {
        return result;
    }

    @Override
    protected RestExternalServiceProviderMetadata createMetadataObject(JsonNode jsonMetadata) {
        return new RestExternalServiceProviderMetadata();
    }
}