package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-02
 */
@Getter
@Setter
public class AccountType extends AbstractAuditableModel<Long> {

    private String name;

}
