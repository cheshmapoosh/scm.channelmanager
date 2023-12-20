package ir.daneshrefah.scm.core.model.condition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;

public class BaseCondition<T> extends BaseModel<T> {
    private AuthenticationMethod authenticationMethod;
    private AuthenticationMethod secondAuthenticationMethod;
    private Condition condition;
    private Boolean status;

    public AuthenticationMethod getAuthenticationMethod() {
        return authenticationMethod;
    }

    public void setAuthenticationMethod(AuthenticationMethod authenticationMethod) {
        this.authenticationMethod = authenticationMethod;
    }

    public AuthenticationMethod getSecondAuthenticationMethod() {
        return secondAuthenticationMethod;
    }

    public void setSecondAuthenticationMethod(AuthenticationMethod secondAuthenticationMethod) {
        this.secondAuthenticationMethod = secondAuthenticationMethod;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
