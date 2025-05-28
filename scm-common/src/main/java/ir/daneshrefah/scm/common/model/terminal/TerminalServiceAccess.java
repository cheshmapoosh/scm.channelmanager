package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.service.ScmService;
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
public class TerminalServiceAccess extends AbstractAuditableModel<Long> {

    private Terminal terminal;
    private ScmService service;

}
