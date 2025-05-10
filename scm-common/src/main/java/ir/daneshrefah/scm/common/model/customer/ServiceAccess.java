package ir.daneshrefah.scm.common.model.customer;

import ir.daneshrefah.scm.common.AuditableModel;
import ir.daneshrefah.scm.common.model.service.Service;
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
public class ServiceAccess extends AuditableModel<Long> {

    private String personProfileId;
    private Service service;
    private Terminal terminal;
    private Object assetId;

}
