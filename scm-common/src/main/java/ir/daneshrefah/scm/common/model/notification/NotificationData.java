package ir.daneshrefah.scm.common.model.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Map<String, Object> valueMap = new HashMap<>();

    public NotificationData put(DataKey key, Object value) {
        valueMap.put(key.getParameterName(), value);
        return this;
    }

    private void put(String key, Object value) {
        valueMap.put(key, value);
    }

    public Object get(DataKey key) {
        return valueMap.get(key.getParameterName());
    }

    @Override
    @SneakyThrows
    public String toString() {
        return OBJECT_MAPPER.writeValueAsString(valueMap);
    }

    @SneakyThrows
    public void fillValues(String json){
        Map<?,?> mapped = OBJECT_MAPPER.readValue(json, HashMap.class);
        mapped
                .keySet()
                .forEach(key->put((String) key,mapped.get(key)));
    }

}
