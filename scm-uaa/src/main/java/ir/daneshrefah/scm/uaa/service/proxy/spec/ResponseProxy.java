package ir.daneshrefah.scm.uaa.service.proxy.spec;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResponseProxy<T> {

    private String contentType;
    private int httpStatusCode;
    private T responseBody;

}
