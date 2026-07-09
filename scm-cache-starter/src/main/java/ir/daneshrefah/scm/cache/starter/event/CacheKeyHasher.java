package ir.daneshrefah.scm.cache.starter.event;

public interface CacheKeyHasher {

    String hash(Object key);

    String type(Object key);
}
