package ir.daneshrefah.scm.utils.validation;

import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.regex.CommonRegex;
import org.apache.commons.collections4.CollectionUtils;

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

    public static void checkNumericInputIfNotNull(Object input,Supplier<RuntimeException> throwsException) {
        if (Objects.nonNull(input)  && StringUtils.isNotNumeric(String.valueOf(input))){
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

    public static void checkNotEqualsObject(Object obj1, Object obj2, Supplier<RuntimeException> throwsException) {
        if (Objects.isNull(obj1) || !obj1.equals(obj2)){
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

    public static void checkInvalidMobileNumber(String mobileNo, Supplier<RuntimeException> throwsException){
        if (!checkIsValidMobileNumber(mobileNo)) {
            throw throwsException.get();
        }
    }

    public static void validateStringLength(String value,Integer length , Supplier<RuntimeException> throwsException){
        if (StringUtils.isEmpty(value)){
            throw throwsException.get();
        }
        if (value.length() < length){
            throw throwsException.get();
        }
    }

    public static void checkEmptyCollection(Collection<?> collectionToValidate, Supplier<RuntimeException> throwsException) {
        if (collectionToValidate == null || collectionToValidate.isEmpty()) {
            throw throwsException.get();
        }
    }

    public static boolean checkIsValidMobileNumber(String code){
        return StringUtils.isNotEmpty(code);
    }

    public static boolean checkIsValidNationalCode(String code){
        //check length
        if (Objects.isNull(code) || code.length() != 10)
            return false;

        long nationalCode = Long.parseLong(code);
        byte[] arrayNationalCode = new byte[10];

        //extract digits from number
        for (int i = 0; i < 10 ; i++) {
            arrayNationalCode[i] = (byte) (nationalCode % 10);
            nationalCode = nationalCode / 10;
        }

        //Checking the control digit
        int sum = 0;
        for (int i = 9; i > 0 ; i--)
            sum += arrayNationalCode[i] * (i+1);
        int temp = sum % 11;
        if (temp < 2)
            return arrayNationalCode[0] == temp;
        else
            return arrayNationalCode[0] == 11 - temp;
    }

}
