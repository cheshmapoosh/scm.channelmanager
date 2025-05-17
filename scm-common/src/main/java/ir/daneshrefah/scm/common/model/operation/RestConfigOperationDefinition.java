package ir.daneshrefah.scm.common.model.operation;

import ir.daneshrefah.scm.common.model.service.HttpMethod;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RestConfigOperationDefinition extends OperationDefinition {
    private String url;
    private HttpMethod httpMethod;
    private Integer responseTimeout;
    private Integer connectTimeout;
    private Integer writeTimeout;
    private Boolean retryEnabled;
    private Integer maxAttempts;
    private Integer minBackoff;
    private Boolean wiretap;
}
