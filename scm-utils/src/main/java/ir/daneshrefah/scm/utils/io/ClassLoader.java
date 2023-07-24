package ir.daneshrefah.scm.utils.io;

import ir.daneshrefah.scm.utils.string.StringUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
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
    public static <T> T createInstanceOfClass(String className, Class<T> clazz, Object... params) {
        if (StringUtils.isEmpty(className))
            return null;
        try {
            Class<T> instanceClass = (Class<T>) Class.forName(className);
            Class<?>[] classes = Arrays.stream(params)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);
            Constructor<T> constructor = instanceClass.getConstructor(classes);
            return constructor.newInstance(params);
        } catch (Exception e) {
            LOGGER.error("error load with class: '{}'", className, e);
        }
        return null;
    }

    public static <T> T createInstanceOfClass(Class<T> clazz, Object... params) {
        try {
            Class<?>[] classes = Arrays.stream(params)
                    .map(Object::getClass)
                    .toArray(Class<?>[]::new);
            Constructor<T> constructor = clazz.getConstructor(classes);
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

}
