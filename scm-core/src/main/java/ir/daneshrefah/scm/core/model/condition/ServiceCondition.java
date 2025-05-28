package ir.daneshrefah.scm.core.model.condition;

import ir.daneshrefah.scm.common.model.service.ScmService;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCondition extends BaseCondition {

    private ScmService service;

}
