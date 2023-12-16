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
    private String terminalCode; // USER_CHANNEL_AUTHENTICATION.CHANNEL_ID
//    private Client client; // USER_CHANNEL_AUTHENTICATION.CHANNEL_ID
    private String nickName; // USER_CHANNEL_AUTHENTICATION.NICK_NAME
    private AuthenticationMethod loginAuthenticationMethod; // USER_CHANNEL_AUTHENTICATION.AUTHENTICATION_METHOD_ID
    private AuthenticationMethod transactionAuthenticationMethod;  // USER_CHANNEL_AUTHENTICATION.SECOND_LEVEL_AUTH_METHOD_ID
    private Boolean active; // USER_CHANNEL_AUTHENTICATION.ACTIVE
    private String firstPassword; // USER_CHANNEL_AUTHENTICATION.FIRST_PASSWORD
    private String secondPassword; // USER_CHANNEL_AUTHENTICATION.SECOND_PASSWORD
    private List<String> accessParameters; // USER_CHANNEL_AUTHENTICATION.CHANNEL_ACCESS_PARAMETER

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(person, user.person) && Objects.equals(terminalCode, user.terminalCode) && Objects.equals(nickName, user.nickName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, terminalCode, nickName);
    }
}
