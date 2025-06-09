package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.service.AssetProviderService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_ASSETS_PROVIDER_FIND_ONE;
import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_ASSET_PROVIDER_LIST;

@Service
public class AssetProviderManagementService extends AbstractJavaService {
    public AssetProviderManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, AssetProviderService assetProviderService) {
        super(producerTemplate, objectMapper);
        this.assetProviderService = assetProviderService;
    }

    private final AssetProviderService assetProviderService;

    @JavaService(operationCode = SVC_ASSET_PROVIDER_LIST)
    @SuppressWarnings("unused")
    public List<AssetProvider> findAllAssetProvider(){
        return assetProviderService.findAssetProviderList();
    }

    @JavaService(operationCode = SVC_ASSETS_PROVIDER_FIND_ONE)
    @SuppressWarnings("unused")
    public AssetProvider findOne(String id){
        ValidationUtils.checkNull(id,()->new InvalidInputException("id"));
        ValidationUtils.checkNumericInput(id,()->new InvalidInputException("id"));
        return assetProviderService.findAssetProviderById(Integer.parseInt(id)).orElseThrow(()->new NoMatchRecordFoundException("id"));
    }

}
