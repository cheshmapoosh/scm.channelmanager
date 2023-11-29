package ir.daneshrefah.scm.cache.config.instances.service;

import com.hazelcast.config.Config;

public interface InstanceConfig {
    void setup(Config config);
}
