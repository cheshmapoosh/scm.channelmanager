package ir.daneshrefah.scm.provider.shetab.iso;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Component
public class ShetabIsoMapConverter {

    public ISOMsg toIsoMsg(Map<String, Object> body) {
        try {
            ISOMsg msg = new ISOMsg();
            Object mti = body.get("mti");
            if (mti != null) {
                msg.setMTI(String.valueOf(mti));
            }

            Map<String, Object> fields = resolveFields(body);
            for (Map.Entry<String, Object> entry : fields.entrySet()) {
                Integer field = parseField(entry.getKey());
                if (field == null || field == 0 || entry.getValue() == null) {
                    continue;
                }
                Object value = entry.getValue();
                if (value instanceof byte[] bytes) {
                    msg.set(field, bytes);
                } else {
                    msg.set(field, String.valueOf(value));
                }
            }
            return msg;
        } catch (ISOException e) {
            throw new IllegalArgumentException("Could not convert map body to ISOMsg", e);
        }
    }

    public Map<String, Object> toMap(ISOMsg msg) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            result.put("mti", msg.hasMTI() ? msg.getMTI() : null);
        } catch (ISOException e) {
            result.put("mti", null);
        }

        Map<String, Object> fields = new TreeMap<>((left, right) -> Integer.compare(Integer.parseInt(left), Integer.parseInt(right)));
        for (Object key : msg.getChildren().keySet()) {
            if (!(key instanceof Integer field) || field == 0) {
                continue;
            }
            byte[] bytes = msg.getBytes(field);
            String value = msg.getString(field);
            fields.put(String.valueOf(field), value != null ? value : bytes);
        }
        result.put("fields", fields);
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveFields(Map<String, Object> body) {
        Object nested = body.get("fields");
        if (nested instanceof Map<?, ?> nestedMap) {
            Map<String, Object> fields = new LinkedHashMap<>();
            nestedMap.forEach((key, value) -> fields.put(String.valueOf(key), value));
            return fields;
        }
        Map<String, Object> fields = new LinkedHashMap<>();
        body.forEach((key, value) -> {
            if (parseField(key) != null) {
                fields.put(key, value);
            }
        });
        return fields;
    }

    private Integer parseField(String key) {
        try {
            return Integer.parseInt(key);
        } catch (Exception ignored) {
            return null;
        }
    }
}
