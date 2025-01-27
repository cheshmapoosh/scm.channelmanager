package ir.daneshrefah.scm.uaa.common.model.user;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.UserStatus;
import ir.daneshrefah.scm.common.model.user.UserType;
import ir.daneshrefah.scm.uaa.common.model.BaseModel;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Data;

import java.util.Objects;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Data
public class User extends BaseModel<Long> {

    private String nickname;
//    private Integer terminalId;
    private String terminalCode;
    private AuthenticationMethod loginAuthenticationMethod;
    private AuthenticationMethod transactionAuthenticationMethod;
    private Set<String> accessParameters;
    private UserStatus status;
    private String loginStaticPassword;
    private String transactionStaticPassword;
    private String otpSerialNumber;
    private GeneralPerson person;
    private UserType type;
    private String creatorBranch;



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(person, user.person) && Objects.equals(terminalCode, user.terminalCode) && Objects.equals(nickname, user.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, terminalCode, nickname);
    }
}
