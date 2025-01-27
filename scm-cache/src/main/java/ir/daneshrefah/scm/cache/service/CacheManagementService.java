package ir.daneshrefah.scm.cache.service;

import ir.daneshrefah.scm.cache.domain.dto.CacheResponse;
import ir.daneshrefah.scm.cache.domain.manage.ClusterInfo;
import ir.daneshrefah.scm.cache.domain.manage.ClusterMember;

public interface CacheManagementService {

    CacheResponse<ClusterInfo> getClusterInfo();

    CacheResponse<ClusterMember> getMembers();
}
