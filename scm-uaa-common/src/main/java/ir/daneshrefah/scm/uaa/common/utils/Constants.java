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

    public final static String LOGIN_HEADER_OTP_CODE = "x-otp-code";
    public final static String LOGIN_HEADER_ACTIVATION_CODE = "RegistryToken";
    public final static String LOGIN_HEADER_CLIENT_SIGNATURE = "Signature";
    public final static String LOGIN_HEADER_CLIENT_VERSION = "AppVersion";


    public static final String CLIENT_SETTING_KEY_TERMINAL_CODE = "terminalCode";

    public final static String CLAIM_KEY_TERMINAL = "terminal";
    public final static String CLAIM_KEY_GRANT = "grant";

//    public final static String CHANNEL_HEADER ="Channel";
//    public final static String AGENT_HEADER ="Agent";
//    public final static String DEVICE_MODEL_HEADER ="DeviceModel";
//    public final static String OPERATING_SYSTEM_VERSION_HEADER ="OsVersion";
//    public final static String UUID_HEADER ="UUID";
//    public final static String HASHCODE_HEADER ="HashCode";
//    public final static String DELEGATED_AUTH_HEADER ="X-DelegateAuthorization";
//    public final static String AUTH_HEADER ="X_UserAuthorization";
//    public final static String IP_HEADER ="X-Forwarded-For";
//    public final static String ACCESS_PARAM_HEADER ="AccessParameter";
//    public final static String FIRST_LVL_AUTH_GRANT="cm-first-password";
//    public final static String SECOND_LVL_AUTH_GRANT="cm-second-password";

}
