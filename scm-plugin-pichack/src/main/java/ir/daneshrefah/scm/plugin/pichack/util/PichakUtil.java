package ir.daneshrefah.scm.plugin.pichack.util;

import ir.daneshrefah.scm.common.constant.TerminalCodes;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
public class PichakUtil {

    public static final String PICHACK_CUSTOMER_AUTH_STATUS_UNAUTHORIZED = "0";
    public static final String PICHACK_CUSTOMER_AUTH_STATUS_AUTHORIZED_1_LEVEL = "1";
    public static final String PICHACK_CUSTOMER_AUTH_STATUS_AUTHORIZED_2_LEVEL = "2";
    public static final String PICHACK_CUSTOMER_AUTH_STATUS_AUTHORIZED_3_LEVEL = "3";
    public static final String PICHACK_CUSTOMER_AUTH_STATUS_AUTHORIZED_4_LEVEL = "4";

    public static String provideBranchCode(Message message) {
        String terminalCode = MessageInputContext.getCurrentContext().getTerminalCode();
        if (TerminalCodes.CMC.equals(terminalCode))
            return null; //TODO
        else
            return terminalCode;
    }

    public static String provideTerminalName(Message message) {
        String terminalCode = MessageInputContext.getCurrentContext().getTerminalCode();
        String clientId = MessageInputContext.getCurrentContext().getClientId();
        TerminalMap map = TerminalMap.findByCode(clientId);
        if (null == map) {
            map = TerminalMap.findByCode(terminalCode);
        }
        return null != map ? map.getCode() : null;
    }

    public static String provideBranchUsername(Message message) {
        String channelCode = MessageInputContext.getCurrentContext().getTerminalCode();
        if (TerminalCodes.CMC.equals(channelCode))
            return null;
        else
            return "";
    }

    public static String provideCustomerAuthStatus(Message message) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication || !authentication.isAuthenticated()) {
            return PICHACK_CUSTOMER_AUTH_STATUS_UNAUTHORIZED;
        }
//        AuthenticationMethod authenticationMethod = authentication.getAuthenticationMethod();
//        if (AuthenticationMethod.STATIC_PASSWORD.equals(authenticationMethod))
//            return PICHACK_CUSTOMER_AUTH_STATUS_AUTHORIZED_1_LEVEL;
//        else
            return PICHACK_CUSTOMER_AUTH_STATUS_AUTHORIZED_2_LEVEL;
    }

    @Getter
    @RequiredArgsConstructor
    private enum TerminalMap {
        BRANCH("BRANCH"), IB("001|IB"), CIB("002|CIB"), IOS("003|IOS"), MB("004|MB"),
        TB("004|TB"), PWA("005|PWA"), SMS("006|SMS"), USD("007|USD"), ATM("008|ATM"),
        KIOSK("009|KIOSK"), SHAPARAK("020|SHAPARAK");

        private final String code;

        public static TerminalMap findByCode(String code) {
            if (StringUtils.isEmpty(code)) {
                return null;
            }
            return Arrays.stream(TerminalMap.values())
                    .filter(m -> StringUtils.containsIgnoreCase(m.code, code))
                    .findFirst()
                    .orElse(null);
        }
    }

}
