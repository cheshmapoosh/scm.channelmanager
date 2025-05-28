package ir.daneshrefah.scm.common.model.customer;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@Getter
@Setter
public class ServiceAccess extends AbstractAuditableModel<Long> {

    private String personProfileId;
    private ScmService service;
    private Terminal terminal;
    private Object assetId;

}
