package ir.daneshrefah.scm.core.model.condition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseCondition extends BaseModel<Long> {

    private Long id;
    private AuthenticationMethod loginAuthenticationMethod;
    private AuthenticationMethod transactionAuthenticationMethod;
    private Condition condition;
    private Boolean status;

}
