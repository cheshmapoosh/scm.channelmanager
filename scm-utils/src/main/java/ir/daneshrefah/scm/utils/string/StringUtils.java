package ir.daneshrefah.scm.utils.string;

import lombok.SneakyThrows;
import org.apache.commons.text.CaseUtils;

import java.net.URI;
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
    public static final String DASH = "-";
    public static final String COLON = ":";
    public static final String DOUBLE_COLON = "::";
    private static final Pattern PATTERN_EXTRACT_PROPERTY = Pattern.compile("\\$\\{(.*?)\\}");

    public static boolean isEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }

    public static boolean isNotEmpty(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isNotEmpty(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        return org.apache.commons.lang3.StringUtils.isBlank(cs);
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

    public static boolean notEquals(CharSequence cs1, CharSequence cs2) {
        return !equals(cs1, cs2);
    }

    public static boolean notEqualsIgnoreCase(CharSequence cs1, CharSequence cs2) {
        return !equalsIgnoreCase(cs1, cs2);
    }

    public static String replaceOnce(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replaceOnce(text, searchString, replacement);
    }

    public static String replace(final String text, final String searchString, final String replacement) {
        return org.apache.commons.lang3.StringUtils.replace(text, searchString, replacement);
    }

    public static String remove(final String text, final String... searchString) {
        String result = text;
        for (String target : searchString) {
            result = org.apache.commons.lang3.StringUtils.remove(result, target);
        }
        return result;
    }

    public static String surroundWithCurlyBracesAndDollar(String value) {
        if (isEmpty(value)) {
            return value;
        }
        return String.format("${%s}", value);
    }

    public static String surroundWithCurlyBraces(String value) {
        if (isEmpty(value)) {
            return value;
        }
        return String.format("{%s}", value);
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

    public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
        return org.apache.commons.lang3.StringUtils.contains(seq, searchSeq);
    }

    public static boolean containsIgnoreCase(final CharSequence seq, final CharSequence searchSeq) {
        return org.apache.commons.lang3.StringUtils.containsIgnoreCase(seq, searchSeq);
    }

    public static boolean containsNone(final CharSequence cs, final String invalidChars) {
        return org.apache.commons.lang3.StringUtils.containsNone(cs, invalidChars);
    }

    public static boolean isNotNumeric(final CharSequence cs) {
        return !isNumeric(cs);
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

    @SneakyThrows
    public static String appendQueryParam(String uri, String name, Object value) {
        if (isEmpty(uri) || isEmpty(name) || null == value) {
            return uri;
        }
        URI oldUri = new URI(uri);
        String appendQuery = name + "=" + value;
        return new URI(oldUri.getScheme(), oldUri.getAuthority(), oldUri.getPath(),
                oldUri.getQuery() == null ? appendQuery : oldUri.getQuery() + "&" + appendQuery, oldUri.getFragment()).toString();
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
     * <pre>
     * {@code
     *  ex. input:      My name is ${name} and im ${age} years old.
     *      return:     name,age (as HashSet)
     * }
     * </pre>
     */
    public static Set<String> findTemplateParameters(String input){
        Set<String> parameters = new HashSet<>();
        char[] charArray = input.toCharArray();
        StringBuilder parameter = new StringBuilder();
        boolean savingChar = false;
        boolean acceptedStartTag = false;
        for (char c : charArray) {
            if (savingChar){
                if (c == '{'){
                    acceptedStartTag = true;
                }else if (c == '}'){
                    parameters.add(parameter.toString());
                    parameter = new StringBuilder();
                    acceptedStartTag = false;
                    savingChar = false;
                }else if (acceptedStartTag){
                    parameter.append(c);
                }
            }
            if (c == '$'){
                savingChar = true;
            }
        }
        return parameters;
    }

    public static String joinWith(final String delimiter, final Object... array) {
        return org.apache.commons.lang3.StringUtils.joinWith(delimiter, array);
    }

    public static String join(List<String> array, final String separator) {
        return org.apache.commons.lang3.StringUtils.join(array, separator);
    }

    public static List<String> extractPropertyNames(String value) {
        List<String> result = new ArrayList<>();
        Matcher matcher = PATTERN_EXTRACT_PROPERTY.matcher(value);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    public static  <T> T compareObject(Object source,Object dest,Class<T> type){
        if (Objects.nonNull(source)){
            return type.cast(source);
        }
        return type.cast(dest);
    }

}
