package ir.daneshrefah.scm.utils.validation;

import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

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

}
