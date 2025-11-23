package ir.daneshrefah.scm.core.services.category;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceCategoryEntity;
import ir.daneshrefah.scm.common.data.mapper.ServiceCategoryMapper;
import ir.daneshrefah.scm.common.data.repository.gateway.ServiceCategoryRepository;
import ir.daneshrefah.scm.common.dto.service.ServiceCategoryRequest;
import ir.daneshrefah.scm.common.dto.service.ServiceCategoryResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.service.category.ServiceCategoryService;
import ir.daneshrefah.scm.task.utils.PageableUtils;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ServiceCategoryServiceImpl implements ServiceCategoryService {

    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCategoryMapper serviceCategoryMapper;

    @Override
    public ServiceCategoryResponse getServiceCategoryById(Short id) {
        ServiceCategoryEntity serviceCategoryEntity = serviceCategoryRepository
                .findById(id).orElseThrow(() -> new NoMatchRecordFoundException("serviceCategory with id " + id + " not found"));
        return serviceCategoryMapper.toDto(serviceCategoryEntity);
    }

    @Override
    public PagedResponseData<ServiceCategoryResponse> getAllServiceCategory(ServiceCategoryRequest request) {
        Pageable pageable = PageableUtils.getPageable(request);
        Page<ServiceCategoryEntity> serviceCategoryEntities = serviceCategoryRepository.findAll(pageable);
        List<ServiceCategoryResponse> serviceCategoryResponses = serviceCategoryMapper.toDtos(serviceCategoryEntities.getContent());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), serviceCategoryEntities.getTotalElements(), serviceCategoryResponses);
    }
}

