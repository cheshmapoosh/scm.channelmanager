package ir.daneshrefah.scm.common.model.message;


import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.common.model.condition.ConditionKey;
import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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

    private final Service service;
    @Setter
    private Map<ConditionKey, Condition> withdrawConditions;
    @Builder.Default
    @Setter
    private int level = 1;
    @Setter
    private String parentMessageId;
    private final String messageId = UUID.randomUUID().toString();
    @Getter
    private final HttpHeader httpHeader = new HttpHeader();
    private final Instant createTime = Instant.now();

    @Getter
    @Setter
    public class HttpHeader{
        private Integer httpStatusCode;
    }

}
