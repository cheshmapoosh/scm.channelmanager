package ir.daneshrefah.scm.common.model.terminal;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public class Terminal {

    private String code;
    private String title;
    private List<TerminalServiceAccess> services;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<TerminalServiceAccess> getServices() {
        return services;
    }

    public void setServices(List<TerminalServiceAccess> services) {
        this.services = services;
    }
}
