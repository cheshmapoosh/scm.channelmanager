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
@DiscriminatorValue(InstanceType.REPLICATED_MAP)
public class ReplicatedMapCacheConfigEntity extends InstanceConfigEntity {
    @Column(name = "ASYNC_FILL_UP_ENABLED")
    private Boolean asyncFillUpEnabled;
    @Column(name = "IN_MEMORY_FORMAT")
    private Integer inMemoryFormat;
    @Column(name = "SPLIT_BRAIN_PROTECTION_NAME")
    private String splitBrainProtectionName;
}
