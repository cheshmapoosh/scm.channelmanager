package ir.daneshrefah.scm.cache.domain.config;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "TBL_CHE_REPLICATED_MAP_CONFIG")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReplicatedMapCacheConfigEntity extends InstanceConfigEntity {
    @Column(name = "ASYNC_FILL_UP_ENABLED")
    private Boolean asyncFillUpEnabled;
    @Column(name = "IN_MEMORY_FORMAT")
    private Integer inMemoryFormat;
    @Column(name = "SPLIT_BRAIN_PROTECTION_NAME")
    private String splitBrainProtectionName;
}
