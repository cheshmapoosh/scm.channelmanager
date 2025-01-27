package ir.daneshrefah.scm.cache.service;

import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.dto.list.ListCacheResponse;
import ir.daneshrefah.scm.cache.domain.dto.list.ListValuesCacheResponse;
import ir.daneshrefah.scm.cache.domain.dto.list.PutListCacheRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;

public interface ListCatchManagementService {

    PagedResponseData<ListCacheResponse> getAllList(CacheFilterRequest request);

    PagedResponseData<ListValuesCacheResponse> getAllValue(CacheFilterRequest request);

    void create(CreateCacheRequest request);

    void put(PutListCacheRequest request);

    void remove(RemoveCacheRequest request);

    void removeValue(RemoveValueCacheRequest request);

    void clear(ClearCacheRequest request);
}
