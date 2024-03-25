package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Data
public class MembershipTerminalAccess extends BaseModel<Long> {

    private Boolean active;
    private Terminal terminal;
    private Membership membership;

}
