package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.AuditableModel;
import ir.daneshrefah.scm.common.model.service.Service;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@Setter
public class TerminalServiceAccess extends AuditableModel<Long> {

    private Terminal terminal;
    private Service service;

}
