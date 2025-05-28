package ir.daneshrefah.scm.common.model.message;


import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.common.model.condition.ConditionKey;
import ir.daneshrefah.scm.common.model.service.ScmService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-23
 */
@Getter
@Builder
public class Header implements Serializable {

    private final ScmService service;
    private final String messageId = UUID.randomUUID().toString();
    @Getter
    private final HttpHeader httpHeader = new HttpHeader();
    private final Instant createTime = Instant.now();
    @Getter
    private final Properties properties = new Properties();
    @Setter
    private Map<ConditionKey, Condition> withdrawConditions;
    @Builder.Default
    @Setter
    private int level = 1;
    @Setter
    private String parentMessageId;

    @Getter
    @Setter
    public static class HttpHeader {
        private Integer httpStatusCode;
    }

    public static class Properties {
        private static final Map<String, Object> PROPERTES_MAP = new ConcurrentHashMap<>();

        public void addProperty(String key, Object value) {
            PROPERTES_MAP.put(key, value);
        }

        public Object getProperty(String key) {
            return PROPERTES_MAP.get(key);
        }

        public List<String> getAllPropertiesName() {
            return PROPERTES_MAP.keySet().stream().toList();
        }

    }


}
