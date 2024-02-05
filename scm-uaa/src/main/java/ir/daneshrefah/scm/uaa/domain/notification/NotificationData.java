package ir.daneshrefah.scm.uaa.domain.notification;

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

    private final Map<String, Object> valueMap = new HashMap<>();

    public NotificationData put(String key, Object value) {
        valueMap.put(key, value);
        return this;
    }

    public Object get(String key) {
        return valueMap.get(key);
    }

}
