package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthenticationMethod extends BaseModel<Integer> {

    private String name;
    private String code;
    private String abbreviation;
}
