package ir.daneshrefah.scm.common.model.condition;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.authentication.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.service.Service;


public class ServiceCondition extends BaseCondition<String> {

    private Service service;


    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }
}
