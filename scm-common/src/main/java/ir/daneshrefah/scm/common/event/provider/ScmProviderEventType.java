package ir.daneshrefah.scm.common.event.provider;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmProviderEventType implements ScmEventType {
    PROVIDER_REQUEST_SENT("provider.request.sent"),
    PROVIDER_RESPONSE_RECEIVED("provider.response.received"),
    PROVIDER_CALL_FAILED("provider.call.failed"),
    PROVIDER_TIMEOUT("provider.timeout");

    private final String code;

    ScmProviderEventType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
