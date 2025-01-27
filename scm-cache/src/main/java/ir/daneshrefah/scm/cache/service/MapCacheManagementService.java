package ir.daneshrefah.scm.cache.service;

import ir.daneshrefah.scm.cache.domain.dto.ClearCacheRequest;
import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.dto.map.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;

public interface MapCacheManagementService {

    PagedResponseData<MapCacheResponse> getAllMaps(CacheFilterRequest request);

    PagedResponseData<MapKeyCacheResponse> getMapKeys(MapKeysCacheFilterRequest request);

    CacheResponse<MapValuesCacheResponse> getMapData(MapValuesCacheRequest request);

    void createMap(CreateCacheRequest request);

    CacheResponse<MapValuesCacheResponse> updateCache(UpdateMapCacheRequest request);

    CacheResponse<MapValuesCacheResponse> put(PutMapCacheRequest request);

    void removeMap(RemoveCacheRequest request);

    void removeMapValue(RemoveValueCacheRequest request);

    void clear(ClearCacheRequest request);
}


