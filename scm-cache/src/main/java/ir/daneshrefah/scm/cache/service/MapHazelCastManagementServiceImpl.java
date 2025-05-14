package ir.daneshrefah.scm.cache.service;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.dto.map.*;
import ir.daneshrefah.scm.cache.exception.DefaultCacheException;
import ir.daneshrefah.scm.cache.utils.PaginationUtils;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MapHazelCastManagementServiceImpl implements MapCacheManagementService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public PagedResponseData<MapCacheResponse> getAllMaps(CacheFilterRequest request) {
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
    public PagedResponseData<MapKeyCacheResponse> getMapKeys(MapKeysCacheFilterRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("mapKey"));
        if (!isMapExists(request.getName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getName()), "1000");
        }
        List<MapKeyCacheResponse> mapKeyCacheResponseList = hazelcastInstance.getMap(request.getName())
                .keySet()
                .stream()
                .map(o -> MapKeyCacheResponse.builder().key(o).build())
                .toList();

        List<MapKeyCacheResponse> listByPagination = PaginationUtils.getListByPagination(mapKeyCacheResponseList, request.getPageNo(), request.getPageSize());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), (long) mapKeyCacheResponseList.size(), listByPagination);
    }

    @Override
    public MapValuesCacheResponse getMapData(MapValuesCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("mapName"));
        ValidationUtils.checkNull(request.getKey(), () -> new MissingRequiredInputException("key"));
        if (!isMapExists(request.getName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getName()), "1000");
        }
        IMap<Object, Object> map = hazelcastInstance.getMap(request.getName());
        if (map.isEmpty() || !map.containsKey(request.getKey())) {
            return null;
        }
        EntryView<Object, Object> entryView = map.getEntryView(request.getKey());
        return MapValuesCacheResponse
                .builder()
                .key(entryView.getKey())
                .value(entryView.getValue())
                .cost(entryView.getCost())
                .creationTime(entryView.getCreationTime() > 0 ? new Date(entryView.getCreationTime()) : null)
                .lastAccessTime(entryView.getLastAccessTime() > 0 ? new Date(entryView.getLastAccessTime()) : null)
                .expirationTime(entryView.getExpirationTime() > 0 ? new Date(entryView.getExpirationTime()) : null)
                .hits(entryView.getHits())
                .lastStoredTime(entryView.getLastStoredTime() > 0 ? new Date(entryView.getLastStoredTime()) : null)
                .lastUpdateTime(entryView.getLastUpdateTime() > 0 ? new Date(entryView.getLastUpdateTime()) : null)
                .version(entryView.getVersion())
                .timeToLive(entryView.getTtl())
                .maxIdle(entryView.getMaxIdle())
                .build();
    }

    @Override
    public void createMap(CreateCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        if (isMapExists(request.getName())) {
            throw new DefaultCacheException(String.format("map [%s] exists", request.getName()), "1001");
        }
        hazelcastInstance.getMap(request.getName());
        hazelcastInstance.getConfig().getMapConfig(request.getName()).setPerEntryStatsEnabled(true);
    }

    @Override
    public MapValuesCacheResponse updateCache(UpdateMapCacheRequest request) {
        if (!isMapExists(request.getName())) {
            throw new DefaultCacheException(String.format("map [%s] exists", request.getName()), "1001");
        }
        EntryView<Object, Object> entryView = hazelcastInstance.getMap(request.getName()).getEntryView(request.getKey());
        ValidationUtils.checkNull(entryView, () -> new DefaultCacheException(String.format("Entry view is null for key [%s] in map [%s]", request.getKey(), request.getName()), "1002"));
        long ttl = request.getTimeToLive() != null ? request.getTimeToLive() : entryView.getTtl();
        long maxIdle = request.getMaxIdle() != null ? request.getMaxIdle() : entryView.getMaxIdle();
        hazelcastInstance.getMap(request.getName())
                .put(request.getKey(), request.getValue(), ttl, request.getTimeToLiveUnit(), maxIdle, request.getMaxIdleUnit());
        return getMapData(new MapValuesCacheRequest(request.getName(), request.getKey()));
    }

    @Override
    public MapValuesCacheResponse put(PutMapCacheRequest request) {
        if (!isMapExists(request.getName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getName()), "1000");
        }
        IMap<Object, Object> map = hazelcastInstance.getMap(request.getName());

        if (request.getTimeToLive() != null && request.getMaxIdle() != null) {
            map.put(request.getKey(), request.getValue(), request.getTimeToLive(), request.getTimeToLiveUnit(), request.getMaxIdle(), request.getMaxIdleUnit());
        } else if (request.getTimeToLive() != null) {
            map.put(request.getKey(), request.getValue(), request.getTimeToLive(), request.getTimeToLiveUnit());
        } else {
            map.put(request.getKey(), request.getValue());
        }
        return getMapData(new MapValuesCacheRequest(request.getName(), request.getKey()));
    }

    @Override
    public void removeMap(RemoveCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        if (!isMapExists(request.getName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getName()), "1000");
        }
        hazelcastInstance.getMap(request.getName()).destroy();
    }

    @Override
    public void removeMapValue(RemoveValueCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        ValidationUtils.checkNull(request.getKey(), () -> new MissingRequiredInputException("key"));
        if (!isMapExists(request.getName())) {
            throw new DefaultCacheException(String.format("map [%s] does not exist", request.getName()), "1000");
        }
        hazelcastInstance.getMap(request.getName()).remove(request.getKey());
    }

    @Override
    public void clear(ClearCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        hazelcastInstance.getMap(request.getName()).clear();
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
