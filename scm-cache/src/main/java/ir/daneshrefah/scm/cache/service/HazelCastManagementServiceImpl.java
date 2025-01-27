package ir.daneshrefah.scm.cache.service;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.cluster.ClusterService;
import com.hazelcast.partition.Partition;
import com.hazelcast.partition.PartitionService;
import ir.daneshrefah.scm.cache.domain.dto.CacheResponse;
import ir.daneshrefah.scm.cache.domain.manage.CachePartition;
import ir.daneshrefah.scm.cache.domain.manage.ClusterInfo;
import ir.daneshrefah.scm.cache.domain.manage.ClusterMember;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-01
 */
@Service
@RequiredArgsConstructor
public class HazelCastManagementServiceImpl implements CacheManagementService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public CacheResponse<ClusterInfo> getClusterInfo() {
        Cluster cluster = hazelcastInstance.getCluster();
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setInstanceName(hazelcastInstance.getName());
        if (cluster instanceof ClusterService) {
            clusterInfo.setClusterId(((ClusterService) cluster).getClusterId() != null ? ((ClusterService) cluster).getClusterId().toString() : null);
            clusterInfo.setMasterAddress(((ClusterService) cluster).getMasterAddress() != null ? ((ClusterService) cluster).getMasterAddress().getHost() : null);
            clusterInfo.setMasterPort(((ClusterService) cluster).getMasterAddress().getPort());
            clusterInfo.setUpTime(DurationFormatUtils.formatDurationWords(((ClusterService) cluster).getClusterClock().getClusterUpTime(), true, true));
            clusterInfo.setClusterTime(Instant.ofEpochMilli(((ClusterService) cluster).getClusterClock().getClusterTime()).toString());
        }
        Collection<Member> members = cluster.getMembers();
        clusterInfo.setMemberCount(members.size());
        PartitionService partitionService = hazelcastInstance.getPartitionService();
        Set<Partition> memberPartitions = partitionService.getPartitions();
        Map<String, List<CachePartition>> groupedByMemberId = memberPartitions.stream().collect(Collectors.groupingBy(partition -> partition.getOwner().getUuid().toString(), Collectors.mapping(partition -> {
            CachePartition result = new CachePartition();
            result.setPartitionId(partition.getPartitionId());
            result.setOwnerMemberId(partition.getOwner().getUuid().toString());
            return result;
        }, Collectors.toList())));
        clusterInfo.setMemberPartitions(groupedByMemberId);
        clusterInfo.setPartitionCount(memberPartitions.size());
        // Create a custom DTO to hold cluster information
        return new CacheResponse<>(List.of(clusterInfo));
    }

    @Override
    public CacheResponse<ClusterMember> getMembers() {
        Cluster cluster = hazelcastInstance.getCluster();
        Collection<Member> members = cluster.getMembers();
        List<ClusterMember> clusterMembers = new ArrayList<>();

        for (Member member : members) {
            ClusterMember clusterMember = new ClusterMember();
            clusterMember.setId(member.getUuid().toString());
            clusterMember.setAddress(member.getAddress().getHost());
            clusterMember.setPort(member.getAddress().getPort());
            clusterMember.setLocalMember(member.localMember());
            clusterMember.setLiteMember(member.isLiteMember());
            clusterMember.setVersion(member.getVersion().toString());
            clusterMembers.add(clusterMember);
        }
        return new CacheResponse<>(clusterMembers);
    }
}
