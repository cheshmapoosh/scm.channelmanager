package ir.daneshrefah.scm.plugin.api.utils;

import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public class HttpUtils {

    public static String extractHeader(HttpServletRequest request, String header) {
        if (null == request || StringUtils.isEmpty(header))
            return null;
        return request.getHeader(header);
    }

    public static String extractTerminalCode(HttpServletRequest request) {
        return extractHeader(request, Constants.SCM_PARAMETER_TERMINAL);
    }

    public static String extractContentType(HttpServletRequest request) {
        return extractHeader(request, HttpConstants.HTTP_HEADER_CONTENT_TYPE);
    }

}
