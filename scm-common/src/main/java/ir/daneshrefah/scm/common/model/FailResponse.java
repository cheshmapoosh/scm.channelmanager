package ir.daneshrefah.scm.common.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FailResponse {
        private int status;
        private String title;
        private String error;
        private String detail;
        private String messageKey;
        private int code;
        private Object message;
}
