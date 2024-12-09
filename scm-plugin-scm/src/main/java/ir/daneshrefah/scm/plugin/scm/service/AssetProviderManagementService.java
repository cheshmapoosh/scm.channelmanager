package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.service.AssetProviderService;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetProviderManagementService extends AbstractJavaService {
    public AssetProviderManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, AssetProviderService assetProviderService) {
        super(producerTemplate, objectMapper);
        this.assetProviderService = assetProviderService;
    }

    private final AssetProviderService assetProviderService;

    @JavaService
    @SuppressWarnings("unused")
    public List<AssetProvider> findAllAssetProvider(){
        return assetProviderService.findAssetProviderList();
    }

    @JavaService
    @SuppressWarnings("unused")
    public AssetProvider findOne(String id){
        ValidationUtils.checkNull(id,()->new InvalidInputException("id"));
        ValidationUtils.checkNumericInput(id,()->new InvalidInputException("id"));
        return assetProviderService.findAssetProviderById(Integer.parseInt(id)).orElseThrow(()->new NoMatchRecordFoundException("id"));
    }

}
