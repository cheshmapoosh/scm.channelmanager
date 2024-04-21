package ir.daneshrefah.scm.utils.validation;

import java.util.Objects;
import java.util.function.Supplier;

public class ValidationUtils {
    private ValidationUtils(){}
    public static void checkNull(Object object,Supplier<? extends Throwable> throwsException){
        if (Objects.isNull(object)){
            throwsException.get();
        }
    }

    public static void checkEmptyString(String string,Supplier<? extends Throwable> throwsException){
        if (Objects.isNull(string) || string.isEmpty()){
            throwsException.get();
        }
    }

    public static void checkBlankString(String string,Supplier<? extends Throwable> throwsException){
        if (Objects.isNull(string) || string.isBlank()){
            throwsException.get();
        }
    }


}
