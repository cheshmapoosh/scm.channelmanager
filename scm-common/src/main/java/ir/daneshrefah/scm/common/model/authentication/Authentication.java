package ir.daneshrefah.scm.common.model.authentication;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.terminal.Terminal;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-14
 */
public abstract class Authentication {

    private String sessionKey;
    private String username;
    private String accessParameter;
    private Terminal terminal;
    private List<Authority> authorities;
//    private List<DelegatedAuthority> delegatedAuthorities;

//    public abstract hasAuthority(Au, User)

}
