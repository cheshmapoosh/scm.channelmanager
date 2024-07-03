package ir.daneshrefah.scm.cache.rest.api;

import ir.daneshrefah.scm.cache.domain.manage.ClusterInfo;
import ir.daneshrefah.scm.cache.service.CacheManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ClusterInfo getClusterInfo() {
        return cacheManagementService.getClusterInfo();
    }

}
