package ir.daneshrefah.scm.cache.config.model;

import com.hazelcast.config.Config;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HazelConfig extends Config {
    private SCM scm;
}
