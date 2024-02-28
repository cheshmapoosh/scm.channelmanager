package ir.daneshrefah.scm.cache.client.distribution.spec;

public interface DistributedJob<T> {
    T accepted();
    T rejected();
}
