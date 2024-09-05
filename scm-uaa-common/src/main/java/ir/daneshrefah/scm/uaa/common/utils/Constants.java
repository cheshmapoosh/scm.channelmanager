package ir.daneshrefah.scm.uaa.common.utils;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-25
 */
public class Constants {

    public static final String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";

    public static final String CLIENT_SETTING_KEY_TERMINAL_CODE = "terminalCode";
    public static final String CLIENT_SETTING_KEY_CHECK_VERSION = "checkVersion";
    public static final String CLIENT_SETTING_KEY_CHECK_ACTIVATION = "checkActivation";
    public static final String CLIENT_SETTING_KEY_ALLOW_IP_ADDRESSES = "allowIpAddresses";

    public final static String CLAIM_KEY_TERMINAL = "trm";
    public final static String CLAIM_KEY_GRANT = "grn";
    public final static String CLAIM_KEY_SESSION = "sni";
    public final static String CLAIM_KEY_LOGIN_AUTH_METHOD = "lam";
    public final static String CLAIM_KEY_TRANSACTION_AUTH_METHOD = "tam";
    public final static String CLAIM_KEY_AUTHORITIES = "aut";
    public final static String CLAIM_KEY_ACCESS_PARAMETER = "acp";
    public final static String CLAIM_KEY_PERSON_TYPE = "pty";
    public final static String CLAIM_KEY_PERSON_NATIONALITY = "pnt";
    public final static String CLAIM_KEY_PERSON_NATIONAL_ID = "pni";
    public final static String CLAIM_KEY_PERSON_SUB_ORGANIZATION_ID = "psi";
    public final static String CLAIM_KEY_PERSON_FIRST_NAME = "pfn";
    public final static String CLAIM_KEY_PERSON_LAST_NAME = "pln";
    public final static String CLAIM_KEY_PERSON_TITLE = "ptl";
    public final static String CLAIM_KEY_PERSON_PROFILE_IDENTIFIER = "ppi";
    public final static String CLAIM_KEY_PERSON_IDENTIFIER = "pid";
    public final static String CLAIM_KEY_USER_CHALLENGE_CODE = "ucc";
    public final static String CLAIM_KEY_TIME_TO_LIVE = "ttl";
    public static final String CLAIM_KEY_MAX_IDLE_TIME = "mit";
    public static final String CLAIM_KEY_PERSON_PHONE_NUMBER = "ppn";



    public final static String OAUTH2_PARAM_NAME_CLIENT_VERSION = "client_version";
    public final static String OAUTH2_PARAM_NAME_CLIENT_SIGNATURE = "client_signature";
    public final static String OAUTH2_PARAM_NAME_CLIENT_AUTHENTICATION = "client_authentication";
    public final static String OAUTH2_PARAM_NAME_USER_USERNAME = "username";
    public final static String OAUTH2_PARAM_NAME_USER_PASSWORD = "password";
    public final static String OAUTH2_PARAM_NAME_MOBILE_NUMBER = "mobileNumber";
    public final static String OAUTH2_PARAM_NAME_USER_CLAIM = "claim_code";
    public final static String OAUTH2_PARAM_NAME_ACCESS_PARAMETER = "access_parameter";
    public final static String OAUTH2_PARAM_NAME_USER_ACTIVATION_CODE = "activation_code";
    public final static String OAUTH2_PARAM_NAME_USER_REGISTER_CODE = "register_code";
    public final static String OAUTH2_PARAM_NAME_USER_TERMINAL = "user_terminal";

    public static final  String OAUTH2_SCOPE_NAME_SESSION = "session";

    public final static String OAUTH2_ERROR_CODE_INVALID_USER = "invalid_user";
    public final static String OAUTH2_ERROR_CODE_INVALID_PASSWORD = "invalid_password";
    public final static String OAUTH2_ERROR_CODE_REQUIRED_CLAIM = "required_claim";
    public final static String OAUTH2_ERROR_CODE_INVALID_CLAIM = "invalid_claim";
    public final static String OAUTH2_ERROR_CODE_INVALID_RATE = "invalid_rate";
    public final static String OAUTH2_ERROR_CODE_INVALID_TRY_COUNT = "invalid_try_count";
    public final static String OAUTH2_ERROR_CODE_IS_LOCKED = "locked_user";
    public final static String OAUTH2_ERROR_CODE_IS_DISABLED = "disabled_user";
    public final static String OAUTH2_ERROR_CODE_IS_EXPIRED = "expired_user";
    public final static String OAUTH2_ERROR_CODE_INVALID_CAPTCHA = "invalid_captcha";

//    public final static String CHANNEL_HEADER ="Channel";
//    public final static String AGENT_HEADER ="Agent";
//    public final static String DEVICE_MODEL_HEADER ="DeviceModel";
//    public final static String OPERATING_SYSTEM_VERSION_HEADER ="OsVersion";
//    public final static String UUID_HEADER ="UUID";
//    public final static String HASHCODE_HEADER ="HashCode";
//    public final static String DELEGATED_AUTH_HEADER ="X-DelegateAuthorization";
//    public final static String AUTH_HEADER ="X_UserAuthorization";
    public final static String IP_HEADER ="X-Forwarded-For";
//    public final static String ACCESS_PARAM_HEADER ="AccessParameter";
//    public final static String FIRST_LVL_AUTH_GRANT="cm-first-password";
//    public final static String SECOND_LVL_AUTH_GRANT="cm-second-password";

    public static final int CIF_PERSON_TYPE_REAL = 50;
    public static final int CIF_PERSON_TYPE_CORPORATE = 4;
    public static final int CIF_PERSON_TYPE_TAMIN = 3;
    public static final int CIF_PERSON_TYPE_BANK = 2;
    public static final int CIF_PERSON_TYPE_GOVERNANCE = 1;
    public static final int CIF_MARITAL_STATUS_MARRIED = 1;
    public static final int CIF_MARITAL_STATUS_SINGLE = 0;
}
