package ir.daneshrefah.scm.cache.starter.utility.resourcelease;

public interface ResourceLease extends AutoCloseable {

    String poolName();

    String resourceName();

    /**
     * Returns the latest locally known ownership state. Unknown ownership is reported as invalid.
     */
    default boolean isValid() {
        return false;
    }

    /**
     * Registers a non-blocking notification invoked when ownership becomes lost or unknown.
     */
    default void onInvalidated(Runnable listener) {
    }

    @Override
    void close();
}
