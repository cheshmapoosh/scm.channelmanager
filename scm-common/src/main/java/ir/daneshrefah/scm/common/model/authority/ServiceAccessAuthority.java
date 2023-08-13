package ir.daneshrefah.scm.common.model.authority;

import ir.daneshrefah.scm.common.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public abstract class ServiceAccessAuthority extends BaseAuthority {

    private Service service;
    private Boolean deny;

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Boolean getDeny() {
        return deny;
    }

    public void setDeny(Boolean deny) {
        this.deny = deny;
    }
}
