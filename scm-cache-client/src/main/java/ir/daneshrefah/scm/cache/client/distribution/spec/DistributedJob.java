package ir.daneshrefah.scm.cache.client.distribution.spec;
@FunctionalInterface
public interface DistributedJob<T> {
    T execute();
}
