package ir.daneshrefah.scm.utils.data;

import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DynamicUpdateUtils {
    private DynamicUpdateUtils() {
    }

    public static void applyChangesIfNotBlank(String input, DynamicChanges<String> dynamicChanges) {
        if (Objects.nonNull(input) && !input.isBlank()) {
            dynamicChanges.apply(input);
        }
    }

    public static <T> void applyChangesIfNotNull( T input, DynamicChanges<T> dynamicChanges) {
        if (Objects.nonNull(input)) {
            dynamicChanges.apply(input);
        }
    }

    public static <T> void applyChangesIfNotEmptyList(List<T> input, DynamicChanges<List<T>> dynamicChanges) {
        if (Objects.nonNull(input) && !input.isEmpty()) {
            dynamicChanges.apply(input);
        }
    }

    public static <T> void applyChangesIfNotEmptySet(Set<T> input, DynamicChanges<Set<T>> dynamicChanges) {
        if (Objects.nonNull(input) && !input.isEmpty()) {
            dynamicChanges.apply(input);
        }
    }
}
