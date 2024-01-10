package ir.daneshrefah.scm.uaa.common.model.user;

import ir.daneshrefah.scm.uaa.common.model.BaseModel;
import ir.daneshrefah.scm.uaa.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Data
public class User extends BaseModel {

    private GeneralPerson person; // USER_CHANNEL_AUTHENTICATION.USER_ID
    private Integer terminalId;
    private String terminalCode;
//    private Client client; // USER_CHANNEL_AUTHENTICATION.CHANNEL_ID
    private String nickname;
    private AuthenticationMethod loginAuthenticationMethod;
    private AuthenticationMethod transactionAuthenticationMethod;
    private Boolean active;
    private String loginStaticPassword;
    private String transactionStaticPassword;
    private List<String> accessParameters;

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
