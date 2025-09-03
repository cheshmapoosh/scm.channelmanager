package ir.daneshrefah.scm.core.services.service;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.data.mapper.EbServiceMapper;
import ir.daneshrefah.scm.common.data.mapper.ServiceCategoryMapper;
import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.dto.service.EbServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.EbServiceFilterRequest;
import ir.daneshrefah.scm.common.dto.service.ServiceCategoryResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.service.ScmServiceService;
import ir.daneshrefah.scm.common.service.category.ServiceCategoryService;
import ir.daneshrefah.scm.core.repository.service.ScmServiceRepository;
import ir.daneshrefah.scm.core.repository.service.ScmServiceSpecification;
import ir.daneshrefah.scm.task.utils.PageableUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class ScmServiceServiceImpl implements ScmServiceService {

    private final ScmServiceRepository scmServiceRepository;
    private final ServiceCategoryService serviceCategoryService;
    private final ServiceCategoryMapper serviceCategoryMapper;
    private final EbServiceMapper ebServiceMapper;

    @Override
    public PagedResponseData<EbService> findServiceList(EbServiceFilterRequest request) {
        Specification<ServiceEntity> specification = ScmServiceSpecification.toSpecification(request);
        Pageable pageable = PageableUtils.getPageable(request);
        Page<ServiceEntity> ebServiceEntities = scmServiceRepository.findAll(specification, pageable);

        List<EbService> ebServices = ebServiceMapper.toModels(ebServiceEntities.getContent());
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), ebServiceEntities.getTotalElements(), ebServices);
    }

    @Override
    public EbService createService(EbServiceCreateRequest request) {
        ValidationUtils.checkNull(request.getServiceCategory(), () -> new MissingRequiredInputException("serviceCategory"));
        ValidationUtils.checkNull(request.getServiceCategory().getId(), () -> new MissingRequiredInputException("serviceCategoryId"));
        ServiceCategoryResponse serviceCategoryResponse = serviceCategoryService.getServiceCategoryById(request.getServiceCategory().getId());
        ServiceEntity serviceEntity = ebServiceMapper.toEntity(request);
        serviceEntity.setServiceCategory(serviceCategoryMapper.toEntity(serviceCategoryResponse));
        scmServiceRepository.save(serviceEntity);
        return ebServiceMapper.toModel(serviceEntity);
    }

    @Override
    public EbService findByServiceId(Short id) {
        ValidationUtils.checkNull(Objects.isNull(id), () -> new MissingRequiredInputException("id"));
        ServiceEntity serviceEntity = scmServiceRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("Service"));
        return ebServiceMapper.toModel(serviceEntity);
    }
}
