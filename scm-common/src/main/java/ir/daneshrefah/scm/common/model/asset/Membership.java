package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.AuditableModel;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Getter
@Setter
public class Membership extends AuditableModel<Long> {
    private String nickname;
    private GeneralPerson person;
    private CustomerAccount customerAccount;
    private Boolean defaultAccount;
    private Integer archiveNumber;
    private Boolean close;

}
