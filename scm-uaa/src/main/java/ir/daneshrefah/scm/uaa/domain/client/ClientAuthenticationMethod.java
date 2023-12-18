package ir.daneshrefah.scm.uaa.domain.client;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-17
 */
public enum ClientAuthenticationMethod {

    CLIENT_SECRET_BASIC,
    CLIENT_SECRET_POST,
    CLIENT_SECRET_JWT,
    PRIVATE_KEY_JWT,
    NONE
}
