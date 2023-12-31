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
        if (!"reza".equals(username) || !"MB".equalsIgnoreCase(terminalCode)) {
            return Optional.empty();
        }
        User user = new User();
        user.setTerminalCode(terminalCode);
        user.setNickName(username);
        user.setFirstPassword("427fb3b98395b962e15831b75c88ad7f");
        user.setActive(true);
        user.setLoginAuthenticationMethod(AuthenticationMethod.STATIC_PASSWORD);
        user.setTransactionAuthenticationMethod(AuthenticationMethod.STATIC_PASSWORD);
        return Optional.of(user);
    }

}
