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
}
