package ir.daneshrefah.scm.common.data.service.bundle;

import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.entity.bundle.ResourceBundleEntity;
import ir.daneshrefah.scm.common.data.mapper.ResourceBundleMapper;
import ir.daneshrefah.scm.common.data.repository.ResourceBundleRepository;
import ir.daneshrefah.scm.common.dto.bundle.BundleCreateRequest;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.exception.RecordVersionException;
import ir.daneshrefah.scm.common.model.bundle.ResourceBundle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResourceBundleAccessService {
    private final ResourceBundleRepository resourceBundleRepository;
    private final ResourceBundleService resourceBundleService;

    public ResourceBundle dynamicUpdate(ResourceBundle resourceBundle){
        ResourceBundleEntity bundle = resourceBundleRepository
                .findByLocaleAndKey(resourceBundle.getLocale(), resourceBundle.getKey())
                .orElseThrow(() -> new NoMatchRecordFoundException("bundle"));
        if (!bundle.getLastEditDate().equals(resourceBundle.getLastEditDate())){
            throw new RecordVersionException("lastEditDate");
        }
        bundle.setValue(resourceBundle.getValue());
        ResourceBundle model = ResourceBundleMapper.INSTANCE.toModel(bundle);
        resourceBundleService.update(model);
        return  model;
    }

    public ResourceBundle create(BundleCreateRequest request) {
        ResourceBundle model = new ResourceBundle();
        model.setKey(request.getKey());
        model.setValue(request.getValue());
        AccessibleLocale locale = AccessibleLocale.findByLocale(request.getLocale()).orElseThrow(() -> new NoMatchRecordFoundException("locale"));
        model.setLocale(locale.getLocale());
        return resourceBundleService.save(model);
    }
}
