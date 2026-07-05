package ir.daneshrefah.scm.common.event.provider;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmProviderEventType implements ScmEventType {
    PROVIDER_REQUEST_SENT,
    PROVIDER_RESPONSE_RECEIVED,
    PROVIDER_CALL_FAILED,
    PROVIDER_TIMEOUT;

    @Override
    public String code() {
        return name();
    }
}
