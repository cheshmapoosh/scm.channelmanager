package ir.daneshrefah.scm.cache.client.utility.resourcelease;

public interface ResourceLease extends AutoCloseable {

    String poolName();

    String resourceName();

    @Override
    void close();
}
