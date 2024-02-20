package ir.daneshrefah.scm.common.model.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */


public class NotificationData {

    @JsonProperty
    private final Map<String, Object> valueMap = new HashMap<>();

    public NotificationData put(DataKey key, Object value) {
        valueMap.put(key.getParameterName(), value);
        return this;
    }

    public Object get(DataKey key) {
        return valueMap.get(key.getParameterName());
    }


}
