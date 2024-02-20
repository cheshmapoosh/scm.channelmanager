package ir.daneshrefah.scm.common.model.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */

@Setter
@Getter
public class NotificationData {

    private final Map<String, Object> valueMap = new HashMap<>();

    public NotificationData put(DataKey key, Object value) {
        valueMap.put(key.getParameterName(), value);
        return this;
    }

    public Object get(DataKey key) {
        return valueMap.get(key.getParameterName());
    }


}
