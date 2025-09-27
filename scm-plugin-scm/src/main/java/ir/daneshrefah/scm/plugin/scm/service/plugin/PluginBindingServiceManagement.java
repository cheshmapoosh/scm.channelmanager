package ir.daneshrefah.scm.plugin.scm.service.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingCreateRequest;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingRequest;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingResponse;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingUpdateRequest;
import ir.daneshrefah.scm.common.model.plugin.PluginHandlerModel;
import ir.daneshrefah.scm.common.service.plugin.PluginResolverService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PluginBindingServiceManagement extends AbstractJavaService {

    private final PluginResolverService pluginResolverService;

    public PluginBindingServiceManagement(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, PluginResolverService pluginResolverService) {
        super(producerTemplate, objectMapper);
        this.pluginResolverService = pluginResolverService;
    }

    @JavaService(operationCode = OperationCode.SVC_PLUGIN_BINDINGS_LIST_BY_DEFINITION_ID)
    public List<PluginBindingResponse> getPluginBindingsByDefinitionId(PluginBindingRequest request) {
        return pluginResolverService.getPluginBindingsByDefinitionId(request);
    }

    @JavaService(operationCode = OperationCode.SVC_PLUGIN_BINDING_CREATE)
    public PluginBindingResponse createPluginBinding(PluginBindingCreateRequest request) {
        return pluginResolverService.createPluginBinding(request);
    }

    @JavaService(operationCode = OperationCode.SVC_PLUGIN_BINDING_UPDATE)
    public PluginBindingResponse updatePluginBinding(PluginBindingUpdateRequest request) {
        return pluginResolverService.updatePluginBinding(request);
    }

    @JavaService(operationCode = OperationCode.SVC_PLUGIN_BINDING_HANDLER_LIST)
    public List<PluginHandlerModel> getPluginHandlerModels() {
        return pluginResolverService.getPluginHandlerModels();
    }
}
