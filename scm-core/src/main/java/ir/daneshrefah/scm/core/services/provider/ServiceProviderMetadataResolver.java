package ir.daneshrefah.scm.core.services.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceProviderMetadataResolver {
    public static final String PROVIDER_META_DATA_PREFIX = "metadata";
    
    @Getter
    @RequiredArgsConstructor
    private enum MetadataProperties {
        SO_TIMEOUT("soTimeout"),
        ENDPOINT("endpoint"),
        CONNECT_TIMEOUT("connectTimeout"),
        RESPONSE_TIMEOUT("responseTimeout"),
        DEFAULT_REQUEST_CONTENT_TYPE("defaultRequestContentType"),
        DEFAULT_HTTP_METHOD("defaultHttpMethod");

        private final String name;
    }

}
