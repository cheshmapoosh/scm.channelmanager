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
@DiscriminatorValue(InstanceType.QUEUE)
public class QueueCacheConfigEntity extends InstanceConfigEntity {
    @Column(name = "BACKUP_COUNT")
    private Integer backupCount;
    @Column(name = "ASYNC_BACKUP_COUNT")
    private Integer asyncBackupCount;
    @Column(name = "MAX_SIZE")
    private Integer maxSize;
    @Column(name = "PRIORITY_COMP_CLASS_NAME")
    private String priorityComparatorClassName;
    @Column(name = "EMPTY_QUEUE_TTL")
    private Integer emptyQueueTtl;
}
