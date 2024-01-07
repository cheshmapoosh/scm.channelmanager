package ir.daneshrefah.scm.utils;

import java.lang.reflect.Constructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-07
 */
public class ClassUtils {

    public static <T extends Throwable> T cloneExceptionWithoutStackTrace(T original) {
        if (null == original)
            return null;
        T newException;
        try {
            Constructor<? extends Throwable> constructor = original.getClass().getConstructor(String.class);
            newException = (T) constructor.newInstance(original.getMessage());
            newException.setStackTrace(new StackTraceElement[]{original.getStackTrace()[0], original.getStackTrace()[1]});
            newException.initCause(cloneExceptionWithoutStackTrace(original.getCause()));
        } catch (Exception e) {
            newException = (T) new Exception(original.getMessage());
            newException.setStackTrace(new StackTraceElement[]{original.getStackTrace()[0], original.getStackTrace()[1]});
            newException.initCause(original.getCause());
        }
        return newException;
    }

}
