package ir.daneshrefah.scm.cache.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SCM_CACHE_SET_CONFIG")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SetCacheConfigEntity extends InstanceConfigEntity {

    @Column(name = "MAX_SIZE")
    private Integer maxSize;
    @Column(name = "ASYNC_BACKUP_COUNT")
    private Integer asyncBackupCount;
    @Column(name = "BACKUP_COUNT")
    private Integer backupCount;

}
