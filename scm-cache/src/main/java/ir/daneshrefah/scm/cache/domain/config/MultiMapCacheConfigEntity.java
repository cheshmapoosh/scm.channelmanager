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
@DiscriminatorValue(InstanceType.MULTIMAP)
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
