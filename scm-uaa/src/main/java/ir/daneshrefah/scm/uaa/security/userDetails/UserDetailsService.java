package ir.daneshrefah.scm.uaa.security.userDetails;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Service
public class UserDetailsService {

    private final UserService userService;

    public UserDetailsService(UserService userService) {
        this.userService = userService;
    }

    public UserDetails loadUserByUsername(String username, String terminalCode) throws UsernameNotFoundException {
        User user = userService.loadUserByUsername(username, terminalCode).orElseThrow(
                () -> new UsernameNotFoundException("invalid username: " + username + ":" + terminalCode));

        return null;
    }

}
