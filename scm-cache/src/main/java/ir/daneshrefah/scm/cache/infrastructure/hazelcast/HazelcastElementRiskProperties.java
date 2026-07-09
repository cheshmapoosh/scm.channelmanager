package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import ir.daneshrefah.scm.observation.starter.element.ScmElementRiskProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.cache.health.hazelcast.element-risk")
public class HazelcastElementRiskProperties extends ScmElementRiskProperties {
}
