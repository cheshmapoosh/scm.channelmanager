package ir.daneshrefah.scm.utils.validation;

import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.regex.CommonRegex;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ValidationUtils {
    private ValidationUtils(){}
    public static void checkNull(Object object,Supplier<RuntimeException> throwsException){
        if (Objects.isNull(object)){
            throw throwsException.get();
        }
    }

    public static void checkNonNull(Object object, Supplier<RuntimeException> throwsException) {
        if (Objects.nonNull(object)) {
            throw throwsException.get();
        }
    }

    public static void checkEmptyString(String string,Supplier<RuntimeException> throwsException){
        if (Objects.isNull(string) || string.isEmpty()){
            throw throwsException.get();
        }
    }

    public static void checkBlankString(String string,Supplier<RuntimeException> throwsException){
        if (Objects.isNull(string) || string.isBlank()){
            throw throwsException.get();
        }
    }

    public static void checkBlankStringIfNotNull(String string,Supplier<RuntimeException> throwsException){
        if (Objects.nonNull(string) && string.isBlank()){
            throw throwsException.get();
        }
    }

    public static void checkNumericInput(Object input,Supplier<RuntimeException> throwsException){
        if (StringUtils.isNotNumeric(String.valueOf(input))){
            throw throwsException.get();
        }
    }

    public static void checkEmptyOptional(Optional<?> input, Supplier<RuntimeException> throwsException) {
        if (Objects.isNull(input) || input.isEmpty()){
            throw throwsException.get();
        }
    }

    public static void checkNullOrEmptyList(List<?> input, Supplier<RuntimeException> throwsException) {
        if (Objects.isNull(input) || input.isEmpty()){
            throw throwsException.get();
        }
    }

    public static void checkNotEqualsString(CharSequence cs1, CharSequence cs2, Supplier<RuntimeException> throwsException) {
        if (StringUtils.notEquals(cs1, cs2)){
            throw throwsException.get();
        }
    }

    public static void checkNotEqualsIgnoreCaseString(CharSequence cs1, CharSequence cs2, Supplier<RuntimeException> throwsException) {
        if (StringUtils.notEqualsIgnoreCase(cs1, cs2)){
            throw throwsException.get();
        }
    }

    public static void checkEqualsIgnoreCaseString(CharSequence cs1, CharSequence cs2, Supplier<RuntimeException> throwsException) {
        if (StringUtils.equalsIgnoreCase(cs1, cs2)){
            throw throwsException.get();
        }
    }

    public static void checkListIsNotEmptyAndNotContains(List list, Object obj, Supplier<RuntimeException> throwsException) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (!list.contains(obj)){
            throw throwsException.get();
        }
    }

    public static void checkListIsNotEmptyAndNotContainsList(List list, Collection obj, Supplier<RuntimeException> throwsException) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (Iterator iterator = obj.iterator(); iterator.hasNext(); ) {
            Object next = iterator.next();
            checkListIsNotEmptyAndNotContains(list, next, throwsException);
        }
    }

    public static void checkRegex(CommonRegex commonRegex, String mobileNumber, Supplier<RuntimeException> throwsException) {
        Pattern pattern = commonRegex.getPattern();
        if (!pattern.matcher(mobileNumber).matches()){
           throw throwsException.get();
        }
    }
}
