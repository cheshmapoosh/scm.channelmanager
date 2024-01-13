package ir.daneshrefah.scm.core.model.condition;

import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCondition extends BaseCondition {

    private Service service;

}
