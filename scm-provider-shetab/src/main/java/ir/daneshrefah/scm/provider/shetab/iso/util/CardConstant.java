package ir.daneshrefah.scm.provider.shetab.iso.util;

public class CardConstant {

    public static final String CARD_ACCEPTOR_BUSINESS_CODE = "6012";
    public static final String DEFAULT_ACQUIRER_INSTITUTION_ID = "589463";
    public static final String DEFAULT_CURRENCY_CODE = "364";
    public static final String DEFAULT_IB_POINT_OF_SERVICE_DATA = "61051061314C";
    private static final String FUNCTION_CODE = "260"; //CM-2137 ->> 260
    private static final String DYNAMIC_PIN_FUNCTION_CODE = "101";
    protected static final String BPG_CARD_ACCEPT_TERMINAL_ID = "10005001";
    protected static final String DEFAULT_CARD_ACCEPT_TERMINAL_ID = "67777777";
    protected static final String DEFAULT_CARD_ACCEPT_ID_CODE = "   777777777600";
    protected static final String IB_CARD_ACCEPT_ID_CODE = "   777777777600";
    protected static final String MB_CARD_ACCEPT_ID_CODE = "   777777777600";
    protected static final String NBK_CARD_ACCEPT_ID_CODE = "000100000005001";
    protected static final String IPG_CARD_ACCEPT_ID_CODE = "   999999999600";
    protected static final String USSD_CARD_ACCEPT_ID_CODE = "   999999999600";
    protected static final String HC_CARD_ACCEPT_ID_CODE = "   999999999600";
    protected static final String IVR_CARD_ACCEPT_ID_CODE = "   888888888602";

    protected static final String DEFAULT_CARD_ACCEPT_NAME_LOCATION = "Refah Bank            Tehran       THRIR010010157171371502184852851";
    public static final String DEFAULT_MB_POINT_OF_SERVICE_DATA = "61051061314C";
    public static final String DEFAULT_POINT_OF_SERVICE_DATA = "61050061313C";
    public static final String DEFAULT_USSD_POINT_OF_SERVICE_DATA = "61050061313C";
    public static final String DEFAULT_IVR_POINT_OF_SERVICE_DATA = "61053061317C";
    private static final String XFER_REV_FUNCTION_CODE = "113";


    public static String getCardAcceptorIdCode(String channelCode) {
        if (channelCode == null || channelCode.isBlank()) {
            return DEFAULT_CARD_ACCEPT_ID_CODE;
        }

        return switch (channelCode.trim().toUpperCase()) {
            case "IB" -> IB_CARD_ACCEPT_ID_CODE;
            case "MB" -> MB_CARD_ACCEPT_ID_CODE;
            case "NBK" -> NBK_CARD_ACCEPT_ID_CODE;
            case "IPG" -> IPG_CARD_ACCEPT_ID_CODE;
            case "USSD" -> USSD_CARD_ACCEPT_ID_CODE;
            case "HC" -> HC_CARD_ACCEPT_ID_CODE;
            case "IVR" -> IVR_CARD_ACCEPT_ID_CODE;
            default -> DEFAULT_CARD_ACCEPT_ID_CODE;
        };
    }

}
