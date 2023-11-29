package ir.daneshrefah.scm.cache.config.instances.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SCM_CACHE_QUEUE_CONFIG")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QueueCacheConfigEntity extends InstanceConfigEntity {
    @Column(name = "BACKUP_COUNT")
    private Integer backupCount;
    @Column(name = "ASYNC_BACKUP_COUNT")
    private Integer asyncBackupCount;
    @Column(name = "MAX_SIZE")
    private Integer maxSize;
    @Column(name = "PRIORITY_COMPARATOR_CLASS_NAME")
    private String priorityComparatorClassName;
    @Column(name = "EMPTY_QUEUE_TTL")
    private Integer emptyQueueTtl;
}
