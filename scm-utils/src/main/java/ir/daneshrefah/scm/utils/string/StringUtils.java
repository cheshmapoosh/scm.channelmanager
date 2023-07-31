package ir.daneshrefah.scm.utils.string;

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

    public static String appendIfMissing(final String str, final CharSequence suffix, final CharSequence... suffixes) {
        return org.apache.commons.lang3.StringUtils.appendIfMissing(str, suffix, suffixes);
    }

}
