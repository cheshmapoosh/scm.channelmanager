package ir.daneshrefah.scm.uaa.security;

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

    public UserDetails loadUserByUsername(String username, String terminalCode) throws UsernameNotFoundException {
        return null;
    }

}
