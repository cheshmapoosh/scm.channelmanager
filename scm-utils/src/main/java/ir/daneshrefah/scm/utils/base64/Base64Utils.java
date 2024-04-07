package ir.daneshrefah.scm.utils.base64;

import java.util.Base64;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
public class Base64Utils {

    public static String encodeWithBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

}
