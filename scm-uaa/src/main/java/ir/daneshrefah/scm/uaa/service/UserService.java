package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.repository.dao.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> loadUserByUsername(String username, String terminalCode) {
        if (!"reza".equals(username) || !"IB".equalsIgnoreCase(terminalCode)) {
            return Optional.empty();
        }
        User user = new User();
        user.setTerminalCode(terminalCode);
        user.setNickName(username);
        user.setFirstPassword("600206cc0021aba85739333707ae33fe");
        user.setActive(true);
        user.setLoginAuthenticationMethod(AuthenticationMethod.STATIC_PASSWORD);
        return Optional.of(user);
    }

}
