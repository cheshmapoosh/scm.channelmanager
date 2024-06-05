package ir.daneshrefah.scm.cache.domain.config;

import com.hazelcast.config.MaxSizePolicy;
import ir.daneshrefah.scm.cache.domain.InstanceType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TBL_CHE_INSTANCE_CONFIG")
@DiscriminatorColumn(name = "INSTANCE_TYPE",discriminatorType =  DiscriminatorType.STRING)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class InstanceConfigEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "INSTANCE_ID")
    private String id;
    @Column(name = "INSTANCE_NAME")
    private String instanceName;
    @Column(name = "STATISTICS_ENABLED")
    private Boolean statisticsEnabled;




//    @Column(name = "INSTANCE_TYPE")
//    private String instanceType;
//
//
//    @Column(name = "TIME_TO_LIVE_SECONDS")
//    private Integer timeToLiveSeconds;
//    @Column(name = "MAX_IDLE_SECONDS")
//    private Integer maxIdleSeconds;
//    @Column(name = "MAX_SIZE")
//    private Integer maxSize;
//    @Column(name = "BACKUP_COUNT")
//    private Integer backupCount;
//    @Column(name = "ASYNC_BACKUP_COUNT")
//    private Integer asyncBackupCount;
//    @Column(name = "EVICTION_SIZE")
//    private Integer evictionSize;
//    @Column(name = "EVICTION_MAX_SIZE_POLICY")
//    private Integer evictionMaxSizePolicy;
//    @Column(name = "MERGE_POLICY_BATCH_SIZE")
//    private Integer mergePolicyBatchSize;
//    @Column(name = "BINARY_ENABLED")
//    private Boolean binaryEnabled;
//    @Column(name = "SPLIT_BRAIN_PROTECTION_NAME")
//    private String splitBrainProtectionName;
//    @Column(name = "VALUE_COLLECTION_TYPE")
//    private String valueCollectionType;
//    @Column(name = "PRIORITY_COMPARATOR_CLASS_NAME")
//    private String priorityComparatorClassName;
//    @Column(name = "EMPTY_QUEUE_TTL")
//    private Integer emptyQueueTtl;
//    @Column(name = "ASYNC_FILL_UP_ENABLED")
//    private Boolean asyncFillUpEnabled;
//    @Column(name = "IN_MEMORY_FORMAT")
//    private Integer inMemoryFormat;
//    @Column(name = "TOPIC_GLOBAL_ORDERING_ENABLED")
//    private Boolean topicGlobalOrderingEnabled;
//    @Column(name = "TOPIC_MULTI_THREADING_ENABLED")
//    private Boolean topicMultiThreadingEnabled;


}
