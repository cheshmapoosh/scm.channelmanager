package ir.daneshrefah.scm.cache.rest.api;

import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.manage.ClusterInfo;
import ir.daneshrefah.scm.cache.domain.manage.ClusterMember;
import ir.daneshrefah.scm.cache.service.CacheManagementService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-01
 */
@RestController
@RequestMapping("/scm-cache-manage")
@RequiredArgsConstructor
public class CacheManagementController {

    private final CacheManagementService cacheManagementService;

    @GetMapping("/cluster-info")
    public CacheResponse<ClusterInfo> getClusterInfo() {
        return cacheManagementService.getClusterInfo();
    }

    @GetMapping("/members/{id}")
    public CacheResponse<ClusterMember> getClusterMembers(@PathVariable("id") String id) {
        return cacheManagementService.getMembers();
    }

    @PostMapping("/maps")
    public PagedResponseData<MapCacheResponse> getAllMaps(@RequestBody MapCacheFilterRequest request) {
        return cacheManagementService.getAllMaps(request);
    }

    @PostMapping("/maps/get-all-map-keys")
    public PagedResponseData<Object> getMapKeys(@RequestBody MapKeysCacheFilterRequest request) {
        return cacheManagementService.getMapKeys(request);
    }

    @PostMapping("/maps/get-all-map-values")
    public CacheResponse<MapValuesCacheResponse> getMapValues(@RequestBody MapValuesCacheRequest request) {
        return cacheManagementService.getMapData(request);
    }

    @PostMapping("/maps/create")
    public ResponseEntity<CreateMapCacheRequest> create(@RequestBody CreateMapCacheRequest request) {
        cacheManagementService.createMap(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/maps/put")
    public CacheResponse<MapValuesCacheResponse> put(@RequestBody PutMapCacheRequest request) {
        return cacheManagementService.putInCache(request);
    }

    @PostMapping("/maps/update")
    public CacheResponse<MapValuesCacheResponse> update(@RequestBody UpdateMapCacheRequest request) {
        return cacheManagementService.updateCache(request);
    }

    @DeleteMapping("/maps/remove")
    public ResponseEntity<RemoveMapCacheRequest> remove(@RequestBody RemoveMapCacheRequest request) {
        cacheManagementService.removeMap(request);
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/maps/remove-value")
    public ResponseEntity<RemoveMapValuesCacheRequest> removeMapValue(@RequestBody RemoveMapValuesCacheRequest request) {
        cacheManagementService.removeMapValue(request);
        return ResponseEntity.ok(request);
    }

    @DeleteMapping("/maps/clear")
    public void clear(@RequestBody ClearMapCacheRequest request) {
        cacheManagementService.clear(request);
    }
}
