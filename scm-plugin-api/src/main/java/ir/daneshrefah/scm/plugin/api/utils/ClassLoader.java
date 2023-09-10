package ir.daneshrefah.scm.plugin.api.utils;

import ir.daneshrefah.scm.utils.string.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class ClassLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassLoader.class);

    private static ApplicationContext applicationContext;

    public static <T> T findBeanOrCreateInstanceOfClass(String className, Class<T> clazz, Object... params) {
        if (StringUtils.startsWith(className, "Bean:", true)) {
            className = StringUtils.replaceOnceIgnoreCase(className, "Bean:", "");
            return  (T) applicationContext.getBean(className);
        } else {
            return  createInstanceOfClass(className, clazz, params);
        }
    }

    public static <T> T createInstanceOfClass(String className, Class<T> clazz, Object... params) {
        if (StringUtils.isEmpty(className))
            return null;
        try {
            Class<T> instanceClass = (Class<T>) Class.forName(className);
//            Class<?>[] classes = Arrays.stream(params)
//                    .map(Object::getClass)
//                    .toArray(Class<?>[]::new);
//            Constructor<T> constructor = instanceClass.getConstructor(classes);
            Constructor<T> constructor = findConstructor(instanceClass, params);
            if (null != constructor)
                return constructor.newInstance(params);
            else
                return null;
        } catch (Exception e) {
            LOGGER.error("error load with class: '{}'", className, e);
        }
        return null;
    }

    public static <T> Constructor<T> findConstructor(Class<T> instanceClass, Object... params) {
        Constructor<T>[] constructors = (Constructor<T>[]) instanceClass.getConstructors();
        for (Constructor<T> constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            boolean matched = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (params.length > i && !parameterType.isAssignableFrom(params[i].getClass())) {
                    matched = false;
                }
            }
            if (matched) {
                return constructor;
            }
        }
        return null;
    }

    public static <T> T createInstanceOfClass(Class<T> clazz, Object... params) {
        try {
//            Class<?>[] classes = Arrays.stream(params)
//                    .map(Object::getClass)
//                    .toArray(Class<?>[]::new);
            Constructor<T> constructor = findConstructor(clazz, params);
            return constructor.newInstance(params);
        } catch (Exception e) {
            LOGGER.error("error load with class: '{}'", clazz.getName(), e);
        }
        return null;
    }

    public static <T> List<Class<? extends T>> loadSubclasses(Class<T> baseClass, String packageName) {
        List<Class<? extends T>> subclasses = new ArrayList<>();

        if (StringUtils.isEmpty(packageName))
            packageName = baseClass.getPackage().getName();

        // Create a Reflections object to scan for classes in the package of the BaseClass
        Reflections reflections = new Reflections(packageName);

        // Get all subtypes of the BaseClass in the specified package and its sub-packages
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(baseClass);

        // Add the subclasses to the list
        subclasses.addAll(subTypes);

        return subclasses;
    }

    public static void setApplicationContext(ApplicationContext applicationContextVal) {
        applicationContext = applicationContextVal;
    }

    public static <T extends Throwable> T cloneExceptionWithoutStackTrace(T original) {
        if (null == original)
            return null;
        T newException;
        try {
            Constructor<? extends Throwable> constructor = original.getClass().getConstructor(String.class);
            newException = (T) constructor.newInstance(original.getMessage());
            newException.setStackTrace(new StackTraceElement[]{original.getStackTrace()[0], original.getStackTrace()[1]});
        } catch (Exception e) {
            newException = (T) new Exception(original.getMessage());
            newException.setStackTrace(new StackTraceElement[]{original.getStackTrace()[0], original.getStackTrace()[1]});
        }
        return newException;
    }
}
