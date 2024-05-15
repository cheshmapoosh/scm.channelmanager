package ir.daneshrefah.scm.utils.data;
@FunctionalInterface
public interface DynamicChanges<T> {
    void apply(T t);
}
