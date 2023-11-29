package ir.daneshrefah.scm.cache.config.instances.domain;

import com.hazelcast.config.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SCM_CACHE_MAP_CONFIG")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MapCacheConfigEntity extends InstanceConfigEntity {

    /**
     * by set this value all data remove from cache after spending the time value as seconds.
     */
    @Column(name = "TIME_TO_LIVE_SECONDS")
    private Integer timeToLiveSeconds;
    @Column(name = "BACK_UP_COUNT")
    private Integer backupCount;
    @Column(name = "MAX_IDLE_SECONDS")
    private Integer maxIdleSeconds;
    /**
     * Maximum number for evicting the cache.
     * default is 1000 , for more information see the
     * MapConfig documentation.
     * notice : by setting this config, you must set evictionMaxSizePolicy value.
     */
    @Column(name = "EVICTION_SIZE")
    private Integer evictionSize;
    /**
     * @see MaxSizePolicy
     * stratagy of evecting like maxheep size or etc.
     * set this value as number in idepent on  MaxSizePolicy.
     */
    @Column(name = "EVICTION_MAX_SIZE_POLICY")
    private Integer evictionMaxSizePolicy;
    @Column(name = "ASYNC_BACKUP_COUNT")
    private Integer asyncBackupCount;

}
