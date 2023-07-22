package ir.daneshrefah.scm.connector.api.component;

import java.util.Map;

public class ScmExchange {

    private Object body;
    private String contentType;
    Map<String, Object> headers;

    public ScmExchange(Object body, String contentType, Map<String, Object> headers) {
        this.body = body;
        this.contentType = contentType;
        this.headers = headers;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
