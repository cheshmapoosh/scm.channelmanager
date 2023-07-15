package ir.daneshrefah.scm.connectorapi.component;

import java.util.Map;

public class ScmExchange {

    private Object body;
    Map<String, Object> headers;

    public ScmExchange(Object body, Map<String, Object> headers) {
        this.body = body;
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
}
