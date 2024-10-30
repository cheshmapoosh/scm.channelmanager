package ir.daneshrefah.scm.common.model.service;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-30
 */
@Data
public class AbstractExternalServiceProviderMetadata {

    protected String endpoint;
    protected Integer connectTimeout;
    protected Integer responseTimeout;
    protected Integer soTimeout;
    @Setter(AccessLevel.NONE)
    private final Map<String, Object> additionalParams = new HashMap<>();

    public void addParam(String key, Object value) {
        if (key == null || key.length() == 0) {
            return;
        }
        additionalParams.put(key, value);
    }

}
