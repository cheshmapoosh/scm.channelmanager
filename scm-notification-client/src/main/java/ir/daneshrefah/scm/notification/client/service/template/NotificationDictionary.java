package ir.daneshrefah.scm.notification.client.service.template;

import ir.daneshrefah.scm.common.model.notification.NotificationData;
import ir.daneshrefah.scm.common.model.notification.NotificationRequest;
import ir.daneshrefah.scm.common.model.notification.constants.NotificationDataKey;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static ir.daneshrefah.scm.utils.string.StringUtils.isBlank;
import static ir.daneshrefah.scm.utils.string.StringUtils.isEmpty;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-01
 */
@RequiredArgsConstructor
@Component
public class NotificationDictionary {

    private static final String DEFAULT_NOT_FOUND_VALUE = "????";
    private final TerminalService terminalService;

    public String extractRequestValue(NotificationRequest request, String key) {
        if (isBlank(key)) {
            return key;
        }
        NotificationDataKey dataKey = NotificationDataKey.findByCode(key);
        if (Objects.nonNull(dataKey) && dataKey.isBuiltIn()) {
            return extractInternalValue(request, dataKey);
        }
        String result = (String) request.getData().get(key);
        return isEmpty(result) ? DEFAULT_NOT_FOUND_VALUE : result;
    }

    private String extractInternalValue(NotificationRequest request, NotificationDataKey dataKey) {
        return switch (dataKey) {
            case TERMINAL_TITLE -> extractTerminalTitle(request.getTerminalCode());
            case PERSON_TITLE -> extractPersonTitle(request.getTerminalCode());
            case USER_NICKNAME -> extractUserNickname(request.getTerminalCode());
            default -> autoMapping(request,dataKey);
        };
    }

    private String extractUserNickname(String terminalCode) {
        return DEFAULT_NOT_FOUND_VALUE;
    }

    private String extractPersonTitle(String terminalCode) {
        return DEFAULT_NOT_FOUND_VALUE;
    }

    private String extractTerminalTitle(String terminalCode) {
        if (isBlank(terminalCode)) {
            return DEFAULT_NOT_FOUND_VALUE;
        }
        return terminalService.findTerminalByCode(terminalCode).map(Terminal::getTitle).orElse("??" + terminalCode + "??");
    }

    private String autoMapping(NotificationRequest request, NotificationDataKey dataKey) {
        try {
            NotificationData data = request.getData();
            return String.valueOf(data.get(dataKey.getCode()));
        }catch (Exception e){
            return DEFAULT_NOT_FOUND_VALUE;
        }
    }
}
