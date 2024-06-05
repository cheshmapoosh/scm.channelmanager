package ir.daneshrefah.scm.common.model.user;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
public enum AuthenticationLevel {

//    ANONYMOUS, CM_REAL, CM_EMPLOYEE, CM_CORPORATE, CLIENT, SMS_VERIFIED, SHAHKAR_VERIFIED, EMAIL_VERIFIED;
    CM_AUTHENTICATED, DELEGATED, SMS_VERIFIED, SHAHKAR_VERIFIED, EMAIL_VERIFIED, ANONYMOUS;

}
