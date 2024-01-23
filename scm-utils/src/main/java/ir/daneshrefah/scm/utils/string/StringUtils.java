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

    public static final String SPACE = org.apache.commons.lang3.StringUtils.SPACE;
    public static final String EMPTY = org.apache.commons.lang3.StringUtils.EMPTY;
    public static final String DOUBLE_COLON = "::";

    public static boolean isEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(cs);
    }

    public static boolean endsWith(final CharSequence str, final CharSequence suffix) {
        return org.apache.commons.lang3.StringUtils.endsWith(str, suffix);
    }

    public static boolean equalsIgnoreCase(CharSequence cs1, CharSequence cs2) {
        return org.apache.commons.lang3.StringUtils.equalsIgnoreCase(cs1, cs2);
    }

    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        return org.apache.commons.lang3.StringUtils.equals(cs1, cs2);
    }

    public static String replaceOnce(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnce(text, searchString, replacement);
    }

    public static String replaceOnceIgnoreCase(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnceIgnoreCase(text, searchString, replacement);
    }

    public static boolean startsWithIgnoreCase(final CharSequence str, final CharSequence prefix) {
        return org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(str, prefix);
    }

    public static boolean startsWith(final CharSequence str, final CharSequence prefix, final boolean ignoreCase) {
        if (ignoreCase) {
            return org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(str, prefix);
        } else {
            return org.apache.commons.lang3.StringUtils.startsWith(str, prefix);
        }
    }

    public static boolean isNumeric(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNumeric(cs);
    }

    public static String appendIfMissing(final String str, final CharSequence suffix, final CharSequence... suffixes) {
        return org.apache.commons.lang3.StringUtils.appendIfMissing(str, suffix, suffixes);
    }  
    
    public static String removeStart(final String str, String remove) {
        return org.apache.commons.lang3.StringUtils.removeStart(str, remove);
    }

    public static String generateGuid() {
//        return RandomStringUtils.random(36, true, true);
        return UUID.randomUUID().toString();
    }

    public static String substringBefore(String str, String separator) {
//        return RandomStringUtils.random(36, true, true);
        return org.apache.commons.lang3.StringUtils.substringBefore(str, separator);
    }

    public static String replaceNull(String value, String replaceWith) {
        if (null == value) {
            return replaceWith;
        }
        return value;
    }

    public static String replaceNullWithSpace(String value) {
        return replaceNull(value, SPACE);
    }

}
