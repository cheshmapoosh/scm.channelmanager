package ir.daneshrefah.scm.cache.client.event;

public interface CacheKeyHasher {

    String hash(Object key);

    String type(Object key);
}
