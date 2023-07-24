package ir.daneshrefah.scm.plugin.api.model.terminal;

import ir.daneshrefah.scm.common.model.BaseModel;
import ir.daneshrefah.scm.plugin.api.model.service.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class TerminalServiceAccess extends BaseModel {

    private Terminal terminal;
    private Service service;

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

}
