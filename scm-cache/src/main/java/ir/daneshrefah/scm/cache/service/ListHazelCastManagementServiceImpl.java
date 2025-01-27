package ir.daneshrefah.scm.cache.service;

import com.hazelcast.collection.IList;
import com.hazelcast.collection.LocalListStats;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.domain.dto.*;
import ir.daneshrefah.scm.cache.domain.dto.list.ListCacheResponse;
import ir.daneshrefah.scm.cache.domain.dto.list.ListValuesCacheResponse;
import ir.daneshrefah.scm.cache.domain.dto.list.PutListCacheRequest;
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
public class ListHazelCastManagementServiceImpl implements ListCatchManagementService {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public PagedResponseData<ListCacheResponse> getAllList(CacheFilterRequest request) {
        List<ListCacheResponse> collect = hazelcastInstance.getDistributedObjects()
                .stream()
                .filter(distributedObject -> distributedObject instanceof IList<?>)
                .map(distributedObject -> hazelcastInstance.getList(distributedObject.getName()))
                .filter(objects  -> Objects.isNull(request) || Objects.isNull(request.getName()) || objects.getName().contains(request.getName()))
                .map(distributedObject -> {
                    IList<Object> list = hazelcastInstance.getList(distributedObject.getName());
                    LocalListStats stats = list.getLocalListStats();
                    return ListCacheResponse.builder()
                            .name(list.getName())
                            .size(list.size())
                            .creationTime(stats.getCreationTime() > 0 ? new Date(stats.getCreationTime()) : null)
                            .lastUpdateTime(stats.getLastUpdateTime() > 0 ? new Date(stats.getLastUpdateTime()) : null)
                            .lastAccessTime(stats.getLastAccessTime() > 0 ? new Date(stats.getLastAccessTime()) : null)
                            .build();
                }).sorted(Comparator.comparing(ListCacheResponse::getName))
                .toList();
        List<ListCacheResponse> listByPagination = PaginationUtils.getListByPagination(collect, request.getPageNo(), request.getPageSize());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), (long) collect.size(), listByPagination);
    }

    @Override
    public PagedResponseData<ListValuesCacheResponse> getAllValue(CacheFilterRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        if (!isExists(request.getName())) {
            throw new RuntimeException();
        }
        List<ListValuesCacheResponse> list = hazelcastInstance.getList(request.getName()).stream().map(value -> ListValuesCacheResponse.builder().value(value).build()).toList();
        List<ListValuesCacheResponse> listByPagination = PaginationUtils.getListByPagination(list, request.getPageNo(), request.getPageSize());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), (long) list.size(), listByPagination);
    }

    @Override
    public void create(CreateCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        if (isExists(request.getName())) {
            throw new RuntimeException();
        }
        hazelcastInstance.getList(request.getName());
        hazelcastInstance.getConfig().getListConfig(request.getName()).setStatisticsEnabled(true);
    }

    @Override
    public void put(PutListCacheRequest request) {
        if (!isExists(request.getName())) {
            throw new RuntimeException();
        }
        IList<Object> list = hazelcastInstance.getList(request.getName());
        list.add(request.getValue());
    }

    @Override
    public void remove(RemoveCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        if (!isExists(request.getName())) {
            throw new RuntimeException();
        }
        hazelcastInstance.getList(request.getName()).destroy();
    }

    @Override
    public void removeValue(RemoveValueCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        ValidationUtils.checkNull(request.getKey(), () -> new MissingRequiredInputException("key"));
        if (!isExists(request.getName())) {
            throw new RuntimeException();
        }
        hazelcastInstance.getList(request.getName()).remove(request.getKey());
    }

    @Override
    public void clear(ClearCacheRequest request) {
        ValidationUtils.checkNull(request.getName(), () -> new MissingRequiredInputException("name"));
        hazelcastInstance.getList(request.getName()).clear();
    }

    public boolean isExists(String listName) {

        for (DistributedObject distributedObject : hazelcastInstance.getDistributedObjects()) {
            if (distributedObject instanceof IList && distributedObject.getName().equals(listName)) {
                return true;
            }
        }
        return false;
    }
}
