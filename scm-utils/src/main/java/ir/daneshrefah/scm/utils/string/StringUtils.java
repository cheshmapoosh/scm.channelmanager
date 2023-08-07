package ir.daneshrefah.scm.utils.string;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class StringUtils {

    public static boolean isEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

    public static boolean endsWith(final CharSequence str, final CharSequence suffix) {
        return org.apache.commons.lang3.StringUtils.endsWith(str, suffix);
    }

    public static String replaceOnce(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnce(text, searchString, replacement);
    }

    public static String replaceOnceIgnoreCase(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnceIgnoreCase(text, searchString, replacement);
    }

    public static boolean startsWith(final CharSequence str, final CharSequence prefix, final boolean ignoreCase) {
        if (ignoreCase) {
            return org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(str, prefix);
        } else {
            return org.apache.commons.lang3.StringUtils.startsWith(str, prefix);
        }
    }
    public static String appendIfMissing(final String str, final CharSequence suffix, final CharSequence... suffixes) {
        return org.apache.commons.lang3.StringUtils.appendIfMissing(str, suffix, suffixes);
    }

    public static String generateGuid() {
//        return RandomStringUtils.random(36, true, true);
        return UUID.randomUUID().toString();
    }

}
