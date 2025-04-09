package ir.daneshrefah.scm.uaa.common.utils;



/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-25
 */
public interface Constants {

    String DEFAULT_ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1";

    String CLIENT_SETTING_KEY_TERMINAL_CODE = "terminalCode";
    String CLIENT_SETTING_KEY_CHECK_VERSION = "checkVersion";
    String CLIENT_SETTING_KEY_CHECK_ACTIVATION = "checkActivation";
    String CLIENT_SETTING_KEY_CHECK_IP_ADDRESS = "checkIpAddress";
    String CLIENT_SETTING_KEY_ALLOW_IP_ADDRESSES = "allowIpAddresses";

    String CLAIM_KEY_TERMINAL = "trm";
    String CLAIM_KEY_GRANT = "grn";
    String CLAIM_KEY_SESSION = "sni";
    String CLAIM_KEY_LOGIN_AUTH_METHOD = "lam";
    String CLAIM_KEY_TRANSACTION_AUTH_METHOD = "tam";
    String CLAIM_KEY_AUTHORITIES = "aut";
    String CLAIM_KEY_ACCESS_PARAMETER = "acp";
    String CLAIM_KEY_PERSON_TYPE = "pty";
    String CLAIM_KEY_PERSON_NATIONALITY = "pnt";
    String CLAIM_KEY_PERSON_NATIONAL_ID = "pni";
    String CLAIM_KEY_PERSON_SUB_ORGANIZATION_ID = "psi";
    String CLAIM_KEY_PERSON_FIRST_NAME = "pfn";
    String CLAIM_KEY_PERSON_LAST_NAME = "pln";
    String CLAIM_KEY_PERSON_TITLE = "ptl";
    String CLAIM_KEY_PERSON_PROFILE_IDENTIFIER = "ppi";
    String CLAIM_KEY_PERSON_IDENTIFIER = "pid";
    String CLAIM_KEY_USER_CHALLENGE_CODE = "ucc";
    String CLAIM_KEY_TIME_TO_LIVE = "ttl";
    String CLAIM_KEY_MAX_IDLE_TIME = "mit";
    String CLAIM_KEY_PERSON_PHONE_NUMBER = "ppn";
    String CLAIM_KEY_ACTIVATOR_TERMINAL_CODE = "atc";


    String OAUTH2_PARAM_NAME_CLIENT_VERSION = "client_version";
    String OAUTH2_PARAM_NAME_CLIENT_SIGNATURE = "client_signature";
    String OAUTH2_PARAM_NAME_CLIENT_AUTHENTICATION = "client_authentication";
    String OAUTH2_PARAM_NAME_USER_USERNAME = "username";
    String OAUTH2_PARAM_NAME_USER_PASSWORD = "password";
    String OAUTH2_PARAM_NAME_MOBILE_NUMBER = "mobileNumber";
    String OAUTH2_PARAM_NAME_USER_CLAIM = "claim_code";
    String OAUTH2_PARAM_NAME_ACCESS_PARAMETER = "access_parameter";
    String OAUTH2_PARAM_NAME_USER_ACTIVATION_CODE = "activation_code";
    String OAUTH2_PARAM_NAME_USER_REGISTER_CODE = "register_code";
    String OAUTH2_PARAM_NAME_USER_TERMINAL = "user_terminal";
    String OAUTH2_PARAM_NAME_ACTIVATOR_TERMINAL = "activator_terminal";

    String OAUTH2_SCOPE_NAME_SESSION = "session";
    String OAUTH2_SCOPE_NAME_ACTIVATION = "activation";
    String OAUTH2_SCOPE_NAME = "scope";

    String OAUTH2_ERROR_CODE_INVALID_USER = "invalid_user";
    String OAUTH2_ERROR_CODE_INVALID_PASSWORD = "invalid_password";
    String OAUTH2_ERROR_CODE_REQUIRED_CLAIM = "required_claim";
    String OAUTH2_ERROR_CODE_INVALID_CLAIM = "invalid_claim";
    String OAUTH2_ERROR_CODE_INVALID_RATE = "invalid_rate";
    String OAUTH2_ERROR_CODE_INVALID_TRY_COUNT = "invalid_try_count";
    String OAUTH2_ERROR_CODE_IS_LOCKED = "locked_user";
    String OAUTH2_ERROR_CODE_IS_DISABLED = "disabled_user";
    String OAUTH2_ERROR_CODE_IS_EXPIRED = "expired_user";
    String OAUTH2_ERROR_CODE_INVALID_CAPTCHA = "invalid_captcha";

    //     String CHANNEL_HEADER ="Channel";
//     String AGENT_HEADER ="Agent";
//     String DEVICE_MODEL_HEADER ="DeviceModel";
//     String OPERATING_SYSTEM_VERSION_HEADER ="OsVersion";
//     String UUID_HEADER ="UUID";
//     String HASHCODE_HEADER ="HashCode";
//     String DELEGATED_AUTH_HEADER ="X-DelegateAuthorization";
//     String AUTH_HEADER ="X_UserAuthorization";
    String IP_HEADER = "X-Forwarded-For";
//     String ACCESS_PARAM_HEADER ="AccessParameter";
//     String FIRST_LVL_AUTH_GRANT="cm-first-password";
//     String SECOND_LVL_AUTH_GRANT="cm-second-password";

    int CIF_PERSON_TYPE_REAL = 50;
    int CIF_PERSON_TYPE_CORPORATE = 4;
    int CIF_PERSON_TYPE_TAMIN = 3;
    int CIF_PERSON_TYPE_BANK = 2;
    int CIF_PERSON_TYPE_GOVERNANCE = 1;
    int CIF_MARITAL_STATUS_MARRIED = 1;
    int CIF_MARITAL_STATUS_SINGLE = 0;
}
