package ir.daneshrefah.scm.common.model.person;

import lombok.Builder;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
// REF.ACCOUNT
@Getter
@Builder
public class Account {

    private AccountType accountType;
    private String accountNo;
    private String nickname;
    private boolean close;

}
