package ir.daneshrefah.scm.uaa.server.security.crypto.password;

public interface UsernamePasswordEncoder {

    String encode(CharSequence rawPassword, String username);


    boolean matches(CharSequence rawPassword, String encodedPassword, String username);
}
