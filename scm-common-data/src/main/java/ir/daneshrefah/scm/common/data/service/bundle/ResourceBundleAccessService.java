package ir.daneshrefah.scm.common.data.service.bundle;

import ir.daneshrefah.scm.common.data.entity.bundle.ResourceBundleEntity;
import ir.daneshrefah.scm.common.data.mapper.ResourceBundleMapper;
import ir.daneshrefah.scm.common.data.repository.ResourceBundleRepository;
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
}
