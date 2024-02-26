package ir.daneshrefah.scm.cache.domain.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TBL_CHE_MULTI_MAP_CONFIG")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MultiMapCacheConfigEntity extends InstanceConfigEntity {
    @Column(name = "BINARY_ENABLED")
    private Boolean binaryEnabled;
    @Column(name = "BACKUP_COUNT")
    private Integer backupCount;
    @Column(name = "ASYNC_BACKUP_COUNT")
    private Integer asyncBackupCount;
    @Column(name = "SPLIT_BRAIN_PROTECTION_NAME")
    private String splitBrainProtectionName;
    @Column(name = "VALUE_COLLECTION_TYPE")
    private String valueCollectionType;
}
