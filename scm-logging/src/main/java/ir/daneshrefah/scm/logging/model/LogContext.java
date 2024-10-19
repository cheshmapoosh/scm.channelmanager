package ir.daneshrefah.scm.logging.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogContext {
    private String correlationId;
    private String messageId;
    private String parentMessageId;
}
