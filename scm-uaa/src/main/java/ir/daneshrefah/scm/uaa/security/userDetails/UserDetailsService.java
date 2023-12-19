package ir.daneshrefah.scm.uaa.security.userDetails;

import ir.daneshrefah.scm.uaa.service.UserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Service
public class UserDetailsService {

    private final Log logger = LogFactory.getLog(getClass());

    private final UserService userService;

    private boolean enableAuthorities = true;

    private boolean enableGroups = true;

    private boolean usernameBasedPrimaryKey = true;

    public UserDetailsService(UserService userService) {
        this.userService = userService;
    }

    public UserDetails loadUserByUsername(String username, String terminalCode) throws UsernameNotFoundException {
        List<TerminalUserDetails> users = loadUsersByUsername(username, terminalCode);
        if (users.size() == 0) {
            this.logger.debug("Query returned no results for user '" + username + ":" + terminalCode + "'");
            throw new UsernameNotFoundException("username not found: '" + username + ":" + terminalCode + "'");
        }
        TerminalUserDetails user = users.get(0); // contains no GrantedAuthority[]
        Set<GrantedAuthority> dbAuthsSet = new HashSet<>();
        if (this.enableAuthorities) {
            dbAuthsSet.addAll(loadUserAuthorities(user.getUsername(), terminalCode));
        }
        if (this.enableGroups) {
            dbAuthsSet.addAll(loadGroupAuthorities(user.getUsername(), terminalCode));
        }
        List<GrantedAuthority> dbAuths = new ArrayList<>(dbAuthsSet);
        addCustomAuthorities(user.getUsername(), dbAuths);
        if (dbAuths.size() == 0) {
            this.logger.debug("User '" + username + "' has no authorities and will be treated as 'not found'");
            throw new UsernameNotFoundException("User '" + username + ":" + terminalCode + "' has no GrantedAuthority");
        }
        return createUserDetails(username, user, dbAuths);
    }

    protected List<TerminalUserDetails> loadUsersByUsername(String username, String terminalCode) {
        Optional<ir.daneshrefah.scm.uaa.common.model.user.User> optionalUser = userService.loadUserByUsername(username, terminalCode);
        if (optionalUser.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(new TerminalUserDetails(optionalUser.get()));
    }

    protected List<GrantedAuthority> loadUserAuthorities(String username, String terminalCode) {
        return Arrays.asList(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    protected List<GrantedAuthority> loadGroupAuthorities(String username, String terminalCode) {
        return Collections.emptyList();
    }

    protected void addCustomAuthorities(String username, List<GrantedAuthority> authorities) {
    }

    protected UserDetails createUserDetails(String username, TerminalUserDetails userFromUserQuery,
                                            List<GrantedAuthority> combinedAuthorities) {
        String returnUsername = userFromUserQuery.getUsername();
        if (!this.usernameBasedPrimaryKey) {
            returnUsername = username;
        }
        return new TerminalUserDetails(userFromUserQuery.getUser(), combinedAuthorities);
    }

}
