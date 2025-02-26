package ir.daneshrefah.scm.cache.rest.api;

import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.dto.list.ListValuesCacheResponse;
import ir.daneshrefah.scm.cache.domain.dto.list.PutListCacheRequest;
import ir.daneshrefah.scm.cache.service.ListCatchManagementService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/scm-cache-manage/list")
@RequiredArgsConstructor
public class ListCacheManagementController {

    private final ListCatchManagementService listCatchManagementService;

    @PostMapping()
    public Object getAllList(@RequestBody CacheFilterRequest request) {
        return listCatchManagementService.getAllList(request);
    }

    @PostMapping("/get-all-value")
    public PagedResponseData<ListValuesCacheResponse> getAllValue(@RequestBody CacheFilterRequest request) {
        return listCatchManagementService.getAllValue(request);
    }

    @PostMapping("/create")
    public ResponseEntity<CreateCacheRequest> create(@RequestBody CreateCacheRequest request) {
        listCatchManagementService.create(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/put")
    public ResponseEntity<PutListCacheRequest> put(@RequestBody PutListCacheRequest request) {
        listCatchManagementService.put(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/remove")
    public ResponseEntity<RemoveCacheRequest> remove(@RequestBody RemoveCacheRequest request) {
        listCatchManagementService.remove(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/remove-value")
    public ResponseEntity<RemoveValueCacheRequest> removeValue(@RequestBody RemoveValueCacheRequest request) {
        listCatchManagementService.removeValue(request);
        return ResponseEntity.ok(request);
    }

    @PostMapping("/clear")
    public void clear(@RequestBody ClearCacheRequest request) {
        listCatchManagementService.clear(request);
    }
}
