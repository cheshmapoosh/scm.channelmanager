package ir.daneshrefah.scm.cache.domain.config;

import ir.daneshrefah.scm.cache.domain.InstanceType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DiscriminatorValue(InstanceType.TOPIC)
public class TopicCacheConfigEntity extends InstanceConfigEntity {
    @Column(name = "TOPIC_GLOBAL_ORD_ENABLED")
    private Boolean topicGlobalOrderingEnabled;
    @Column(name = "TOPIC_MULTI_THRD_ENABLED")
    private Boolean topicMultiThreadingEnabled;
}
