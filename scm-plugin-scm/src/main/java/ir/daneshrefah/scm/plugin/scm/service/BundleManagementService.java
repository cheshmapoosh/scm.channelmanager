package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleAccessService;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;
import ir.daneshrefah.scm.common.service.bundle.BundleEditRequest;
import ir.daneshrefah.scm.common.service.bundle.BundleFindRequest;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BundleManagementService extends AbstractJavaService {

    private final ResourceBundleService resourceBundleService;
    private final ResourceBundleAccessService resourceBundleAccessService;

    public BundleManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper,
                                   ResourceBundleService resourceBundleService,
                                   ResourceBundleAccessService resourceBundleAccessService) {
        super(producerTemplate, objectMapper);
        this.resourceBundleService = resourceBundleService;
        this.resourceBundleAccessService = resourceBundleAccessService;
    }

    @JavaService
    public PagedResponseData<ResourceBundle> bundleList(BundleFindRequest request) {
        List<ResourceBundle> bundleList = resourceBundleService.getAll().stream()
                .filter(bundle -> null == request || null == request.getKey() || bundle.getKey().toLowerCase().contains(request.getKey().toLowerCase()))
                .filter(bundle -> null == request || null == request.getValue() || bundle.getValue().toLowerCase().contains(request.getValue().toLowerCase()))
                .filter(bundle -> null == request || null == request.getLocale() || AccessibleLocale.findByLocale(request.getLocale()).equals(AccessibleLocale.findByLocale(bundle.getLocale())))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, bundleList);
    }

    @JavaService
    public ResourceBundle findById(String id) {
        if (Objects.isNull(id) || id.isBlank() || !StringUtils.isNumeric(id)) {
            throw new InvalidInputException("id");
        }
        return resourceBundleService
                .getAll()
                .stream()
                .filter(resourceBundle -> resourceBundle.getId().equals(Long.parseLong(id)))
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException("id"));
    }

    @JavaService
    public ResourceBundle edit(BundleEditRequest request) {
        validateBundleEditRequest(request);
        ResourceBundle foundById = findById(request.getId().toString());
        ResourceBundle resourceBundle = new ResourceBundle();
        resourceBundle.setKey(foundById.getKey());
        resourceBundle.setValue(request.getValue());
        resourceBundle.setLocale(foundById.getLocale());
        resourceBundle.setLastEditDate(request.getLastEditDate());
        return resourceBundleAccessService.dynamicUpdate(resourceBundle);
    }

    private void validateBundleEditRequest(BundleEditRequest request) {
        Long id = request.getId();
        LocalDateTime lastEditDate = request.getLastEditDate();
        String value = request.getValue();
        ValidationUtils.checkBlankStringIfNotNull(value,()->new InvalidInputException("value"));
        ValidationUtils.checkNull(id,()->new InvalidInputException("id"));
        ValidationUtils.checkNumericInput(id,()->new InvalidInputException("id"));
        ValidationUtils.checkNull(lastEditDate,()->new InvalidInputException("lastEditDate"));
    }

}
