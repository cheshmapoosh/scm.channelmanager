package ir.daneshrefah.scm.cache.service;

import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.manage.ClusterInfo;
import ir.daneshrefah.scm.cache.domain.manage.ClusterMember;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-01
 */
public interface CacheManagementService {

    CacheResponse<ClusterInfo> getClusterInfo();

    PagedResponseData<MapCacheResponse> getAllMaps(MapCacheFilterRequest request);

    PagedResponseData<Object> getMapKeys(MapKeysCacheFilterRequest request);

    CacheResponse<MapValuesCacheResponse> getMapData(MapValuesCacheRequest request);

    void createMap(CreateMapCacheRequest request);

    CacheResponse<MapValuesCacheResponse> updateCache(UpdateMapCacheRequest request);

    CacheResponse<MapValuesCacheResponse> putInCache(PutMapCacheRequest request);

    void removeMap(RemoveMapCacheRequest request);

    void removeMapValue(RemoveMapValuesCacheRequest request);

    void clear(ClearMapCacheRequest request);

    CacheResponse<ClusterMember> getMembers();

}
