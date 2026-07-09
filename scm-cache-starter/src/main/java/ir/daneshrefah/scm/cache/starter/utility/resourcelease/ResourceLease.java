package ir.daneshrefah.scm.cache.starter.utility.resourcelease;

public interface ResourceLease extends AutoCloseable {

    String poolName();

    String resourceName();

    @Override
    void close();
}
