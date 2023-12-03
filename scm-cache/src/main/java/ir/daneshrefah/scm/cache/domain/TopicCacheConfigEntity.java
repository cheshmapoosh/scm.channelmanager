package ir.daneshrefah.scm.cache.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TBL_CHE_TOPIC_CONFIG")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TopicCacheConfigEntity extends InstanceConfigEntity {
    @Column(name = "GLOBAL_ORDERING_ENABLED")
    private Boolean globalOrderingEnabled;
    @Column(name = "MULTI_THREADING_ENABLED")
    private Boolean multiThreadingEnabled;
}
