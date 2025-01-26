package ir.daneshrefah.scm.cache.service;

import com.hazelcast.cluster.Cluster;
import com.hazelcast.cluster.Member;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.cluster.ClusterService;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import com.hazelcast.partition.Partition;
import com.hazelcast.partition.PartitionService;
import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.manage.CachePartition;
import ir.daneshrefah.scm.cache.domain.manage.ClusterInfo;
import ir.daneshrefah.scm.cache.domain.manage.ClusterMember;
import ir.daneshrefah.scm.cache.exception.DefaultCacheException;
import ir.daneshrefah.scm.cache.utils.PaginationUtils;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
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

    @Override
    public PagedResponseData<MapCacheResponse> getAllMaps(MapCacheFilterRequest request) {
        List<MapCacheResponse> collect = hazelcastInstance.getDistributedObjects()
                .stream()
                .filter(distributedObject -> distributedObject instanceof IMap<?, ?>)
                .map(distributedObject -> hazelcastInstance.getMap(distributedObject.getName()))
                .filter(entries -> Objects.isNull(request) || Objects.isNull(request.getName()) || entries.getName().contains(request.getName()))
                .map(distributedObject -> {
                    IMap<?, ?> map = hazelcastInstance.getMap(distributedObject.getName());
                    LocalMapStats stats = map.getLocalMapStats();
                    return MapCacheResponse.builder()
                            .name(distributedObject.getName())
                            .size(map.size())
                            .hits(stats.getHits())
                            .heapCost(stats.getHeapCost())
                            .creationTime(stats.getCreationTime() > 0 ? new Date(stats.getCreationTime()) : null)
                            .lastUpdateTime(stats.getLastUpdateTime() > 0 ? new Date(stats.getLastUpdateTime()) : null)
                            .lastAccessTime(stats.getLastAccessTime() > 0 ? new Date(stats.getLastAccessTime()) : null)
                            .build();
                }).sorted(Comparator.comparing(MapCacheResponse::getName))
                .toList();
        List<MapCacheResponse> listByPagination = PaginationUtils.getListByPagination(collect, request.getPageNo(), request.getPageSize());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), (long) collect.size(), listByPagination);
    }

    @Override
    public PagedResponseData<Object> getMapKeys(MapKeysCacheFilterRequest request) {
        ValidationUtils.checkNull(request.getMapName(), () -> new MissingRequiredInputException("mapKey"));
        if (!isMapExists(request.getMapName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getMapName()), "1000");
        }
        Set<Object> sets = hazelcastInstance.getMap(request.getMapName()).keySet();
        List<Object> lists = new ArrayList<>(sets);
        List<Object> listByPagination = PaginationUtils.getListByPagination(lists, request.getPageNo(), request.getPageSize());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), (long) sets.size(), listByPagination);
    }

    @Override
    public CacheResponse<MapValuesCacheResponse> getMapData(MapValuesCacheRequest request) {
        ValidationUtils.checkNull(request.getMapName(), () -> new MissingRequiredInputException("mapName"));
        ValidationUtils.checkNull(request.getKey(), () -> new MissingRequiredInputException("key"));
        if (!isMapExists(request.getMapName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getMapName()), "1000");
        }
        IMap<Object, Object> map = hazelcastInstance.getMap(request.getMapName());
        if (map.isEmpty() || !map.containsKey(request.getKey())) {
            return new CacheResponse<>(List.of());
        }
        EntryView<Object, Object> entryView = map.getEntryView(request.getKey());
        MapValuesCacheResponse mapValuesCacheResponse = MapValuesCacheResponse.builder().key(entryView.getKey()).value(entryView.getValue()).cost(entryView.getCost()).creationTime(entryView.getCreationTime()).lastAccessTime(entryView.getLastAccessTime()).expirationTime(entryView.getExpirationTime()).hits(entryView.getHits()).lastStoredTime(entryView.getLastStoredTime()).lastUpdateTime(entryView.getLastUpdateTime()).version(entryView.getVersion()).ttl(entryView.getTtl()).maxIdle(entryView.getMaxIdle()).build();
        return new CacheResponse<>(List.of(mapValuesCacheResponse));
    }

    @Override
    public void createMap(CreateMapCacheRequest request) {
        ValidationUtils.checkNull(request.getMapName(), () -> new MissingRequiredInputException("mapName"));
        if (isMapExists(request.getMapName())) {
            throw new DefaultCacheException(String.format("map [%s] exists", request.getMapName()), "1001");
        }
        hazelcastInstance.getMap(request.getMapName());
        hazelcastInstance.getConfig().getMapConfig(request.getMapName()).setPerEntryStatsEnabled(true);
    }

    @Override
    public CacheResponse<MapValuesCacheResponse> updateCache(UpdateMapCacheRequest request) {
        if (!isMapExists(request.getMapName())) {
            throw new DefaultCacheException(String.format("map [%s] exists", request.getMapName()), "1001");
        }
        EntryView<Object, Object> entryView = hazelcastInstance.getMap(request.getMapName()).getEntryView(request.getKey());
        ValidationUtils.checkNull(entryView, () -> new DefaultCacheException(String.format("Entry view is null for key [%s] in map [%s]", request.getKey(), request.getMapName()), "1002"));
        long ttl = request.getTimeToLive() != null ? request.getTimeToLive() : entryView.getTtl();
        long maxIdle = request.getMaxIdle() != null ? request.getMaxIdle() : entryView.getMaxIdle();
        hazelcastInstance.getMap(request.getMapName())
                .put(request.getKey(), request.getValue(), ttl, request.getTimeToLiveUnit(), maxIdle, request.getMaxIdleUnit());
        return getMapData(new MapValuesCacheRequest(request.getMapName(), request.getKey()));
    }

    public CacheResponse<MapValuesCacheResponse> putInCache(PutMapCacheRequest request) {
        if (!isMapExists(request.getMapName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getMapName()), "1000");
        }
        IMap<Object, Object> map = hazelcastInstance.getMap(request.getMapName());

        if (request.getTimeToLive() != null && request.getMaxIdle() != null) {
            map.put(request.getKey(), request.getValue(), request.getTimeToLive(), request.getTimeToLiveUnit(), request.getMaxIdle(), request.getMaxIdleUnit());
        } else if (request.getTimeToLive() != null) {
            map.put(request.getKey(), request.getValue(), request.getTimeToLive(), request.getTimeToLiveUnit());
        } else {
            map.put(request.getKey(), request.getValue());
        }
        return getMapData(new MapValuesCacheRequest(request.getMapName(), request.getKey()));
    }

    @Override
    public void removeMap(RemoveMapCacheRequest request) {
        ValidationUtils.checkNull(request.getMapName(), () -> new MissingRequiredInputException("mapKey"));
        if (!isMapExists(request.getMapName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getMapName()), "1000");
        }
        hazelcastInstance.getMap(request.getMapName()).destroy();
    }

    @Override
    public void removeMapValue(RemoveMapValuesCacheRequest request) {
        ValidationUtils.checkNull(request.getMapName(), () -> new MissingRequiredInputException("mapKey"));
        ValidationUtils.checkNull(request.getKey(), () -> new MissingRequiredInputException("key"));
        if (!isMapExists(request.getMapName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getMapName()), "1000");
        }
        hazelcastInstance.getMap(request.getMapName()).remove(request.getKey());
    }

    public void clear(ClearMapCacheRequest request) {
        ValidationUtils.checkNull(request.getMapName(), () -> new MissingRequiredInputException("mapKey"));
        hazelcastInstance.getMap(request.getMapName()).clear();
    }

    public boolean isMapExists(String mapName) {
        for (DistributedObject distributedObject : hazelcastInstance.getDistributedObjects()) {
            if (distributedObject instanceof IMap && distributedObject.getName().equals(mapName)) {
                return true;
            }
        }
        return false;
    }
}
