package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleAccessService;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.dto.bundle.BundleCreateRequest;
import ir.daneshrefah.scm.common.dto.bundle.BundleEditRequest;
import ir.daneshrefah.scm.common.dto.bundle.BundleFindRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BundleManagementService extends AbstractJavaService {

    private static final Logger log = LoggerFactory.getLogger(BundleManagementService.class);
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
        ResourceBundle foundById = findById(request.getId().toString());
        ResourceBundle resourceBundle = new ResourceBundle();
        resourceBundle.setKey(foundById.getKey());
        resourceBundle.setValue(request.getValue());
        resourceBundle.setLocale(foundById.getLocale());
        resourceBundle.setLastEditDate(request.getLastEditDate());
        return resourceBundleAccessService.dynamicUpdate(resourceBundle);
    }

    @JavaService
    public ResourceBundle create(BundleCreateRequest request){
        return resourceBundleAccessService.create(request);
    }

    @JavaService
    public List<String> getAllAvailableLocales(){
        return Arrays.stream(AccessibleLocale.values())
                .filter(locale -> !locale.equals(AccessibleLocale.DEFAULT_LOCALE))
                .map(locale -> locale.getLanguageCode() + "-" + locale.getCountryCode())
                .collect(Collectors.toList());
    }


}
