package ir.daneshrefah.scm.uaa.service.activation.pwa.model;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Data
@Accessors(chain = true)
public class ActivationResponse {
    private String detail;
    private int code;
    private String messageKey;
    private String text;
    private HttpStatus httpCode;
    private String data;
}
