package ir.daneshrefah.scm.cache.rest.api;

import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.dto.map.*;
import ir.daneshrefah.scm.cache.service.MapCacheManagementService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scm-cache-manage/maps")
@RequiredArgsConstructor
public class MapCacheManagementController {

    private final MapCacheManagementService mapCacheManagementService;

    @PostMapping()
    public PagedResponseData<MapCacheResponse> getAllMaps(@RequestBody CacheFilterRequest request) {
        return mapCacheManagementService.getAllMaps(request);
    }

    @PostMapping("/get-all-map-keys")
    public PagedResponseData<MapKeyCacheResponse> getMapKeys(@RequestBody MapKeysCacheFilterRequest request) {
        return mapCacheManagementService.getMapKeys(request);
    }

    @PostMapping("/get-all-map-values")
    public CacheResponse<MapValuesCacheResponse> getMapValues(@RequestBody MapValuesCacheRequest request) {
        return mapCacheManagementService.getMapData(request);
    }

    @PostMapping("/create")
    public ResponseEntity<CreateCacheRequest> create(@RequestBody CreateCacheRequest request) {
        mapCacheManagementService.createMap(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/put")
    public CacheResponse<MapValuesCacheResponse> put(@RequestBody PutMapCacheRequest request) {
        return mapCacheManagementService.put(request);
    }

    @PostMapping("/update")
    public CacheResponse<MapValuesCacheResponse> update(@RequestBody UpdateMapCacheRequest request) {
        return mapCacheManagementService.updateCache(request);
    }

    @PostMapping("/remove")
    public ResponseEntity<RemoveCacheRequest> remove(@RequestBody RemoveCacheRequest request) {
        mapCacheManagementService.removeMap(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/remove-value")
    public ResponseEntity<RemoveValueCacheRequest> removeMapValue(@RequestBody RemoveValueCacheRequest request) {
        mapCacheManagementService.removeMapValue(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/clear")
    public void clear(@RequestBody ClearCacheRequest request) {
        mapCacheManagementService.clear(request);
    }
}
