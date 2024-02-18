package ir.daneshrefah.scm.utils.string;

import org.apache.commons.text.CaseUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String leftPadZero(final String str, final int size) {
        return leftPad(str, size, '0');
    }

    public static String leftPad(final String str, final int size, final char padChar) {
        return org.apache.commons.lang3.StringUtils.leftPad(str, size, padChar);
    }

    public static String replaceOnceIgnoreCase(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnceIgnoreCase(text, searchString, replacement);
    }

    public static boolean startsWithIgnoreCase(final CharSequence str, final CharSequence prefix) {
        return org.apache.commons.lang3.StringUtils.startsWithIgnoreCase(str, prefix);
    }

    public static String toCamelCase(String str) {
        return toCamelCase(str, true, '_');
    }

    public static String toCamelCase(String str, final boolean capitalizeFirstLetter, final char... delimiters) {
        return CaseUtils.toCamelCase(str, capitalizeFirstLetter, delimiters);
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

    public static String cleanUpJsonCharacters(String json) {
        if (json.startsWith("\"") && json.endsWith("\"")) {
            //remove start and end quotations
            json = json.substring(1, json.length() - 1);
        }
        //remove text gap
        json = json.replace(" ", "");
        if (json.contains("\\n") || json.contains("\\r")) {
            //remove \n\r characters
            json = json.replace("\\n", "").replace("\\r", "");
        }
        //clean slash chars
        json = json.replace("\\", "");
        return json;
    }

    /**
     * @apiNote this method find list of parameters from input text. for example if input "hello {name},what is your{phone}"
     * the return list is "name,phone"
     * @return found parameters
     */
    public static List<String> findAllParameters(String input){
        Pattern pattern = Pattern.compile("\\{(.*?)}");
        Matcher matcher = pattern.matcher(input);
        List<String> parameters = new ArrayList<>();
        while (matcher.find()) {
            parameters.add(matcher.group(1));
        }
        return parameters;
    }

    /**
     *
     * @param input
     * this method find all template parameters.
     * ex. My name is ${name} and im ${age} years old.
     * return : [{name},{age}] as HashSet
     */
    public static Set<String> findTemplateParameters(String input){
        Pattern pattern = Pattern.compile("\\$\\{(.*?)}");
        Matcher matcher = pattern.matcher(input);
        Set<String> parameters = new HashSet<>();
        while (matcher.find()) {
            parameters.add(matcher.group(1));
        }
        return parameters;
    }

    public static String join(List<String> array, final String separator) {
        return org.apache.commons.lang3.StringUtils.join(array, separator);
    }

}
