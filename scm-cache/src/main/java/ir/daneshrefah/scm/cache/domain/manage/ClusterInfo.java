package ir.daneshrefah.scm.cache.domain.manage;

import lombok.Data;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-01
 */
@Data
public class ClusterInfo {

    private String instanceName;
    private String clusterId;
    private String masterAddress;
    private Integer masterPort;
    private String clusterTime;
    private String upTime;
    private int memberCount;
    private Collection<ClusterMember> members;
    private int partitionCount;
    private Map<String, List<CachePartition>> memberPartitions;

}
