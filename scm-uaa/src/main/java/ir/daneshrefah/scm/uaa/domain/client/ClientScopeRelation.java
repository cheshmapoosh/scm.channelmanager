package ir.daneshrefah.scm.uaa.domain.client;

import ir.daneshrefah.scm.common.AuditableModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-04
 */
@Getter
@Setter
public class ClientScopeRelation extends AuditableModel<Long> {

    private Client client;
    private Scope scope;

}
