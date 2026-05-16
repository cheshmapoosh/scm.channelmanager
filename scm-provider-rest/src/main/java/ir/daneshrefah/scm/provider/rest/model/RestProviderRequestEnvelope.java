package ir.daneshrefah.scm.provider.rest.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RestProviderRequestEnvelope {
    private String method;
    private String url;
    private String path;
    private Map<String, Object> headers;
    private Map<String, Object> query;
    private Object body;
    private Auth auth;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Auth {
        private String type;
        private String headerName;
        private String prefix;
        private String token;
        private String username;
        private String password;
        private Boolean basicBase64;
    }
}
