package ir.daneshrefah.scm.uaa.repository.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.net.URI;

/**
 * Persists a configured resource location without reducing the domain model to a transport-specific URL.
 */
@Converter
public class UriAttributeConverter implements AttributeConverter<URI, String> {

    private static final int MAX_URI_LENGTH = 512;

    @Override
    public String convertToDatabaseColumn(URI attribute) {
        if (attribute == null) {
            return null;
        }
        validate(attribute);
        return attribute.toString();
    }

    @Override
    public URI convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            URI uri = URI.create(dbData);
            validate(uri);
            return uri;
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("JWK_SET_URI contains an invalid URI", exception);
        }
    }

    private void validate(URI uri) {
        String value = uri.toString();
        if (value.isBlank() || value.length() > MAX_URI_LENGTH || uri.getScheme() == null) {
            throw new IllegalArgumentException("JWK_SET_URI must be an absolute URI of at most 512 characters");
        }
    }
}
