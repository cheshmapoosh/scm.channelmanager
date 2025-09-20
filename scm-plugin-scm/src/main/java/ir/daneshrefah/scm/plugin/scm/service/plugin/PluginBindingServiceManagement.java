package ir.daneshrefah.scm.plugin.scm.service.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.model.plugin.PluginBindingCreateRequest;
import ir.daneshrefah.scm.common.model.plugin.PluginBindingRequest;
import ir.daneshrefah.scm.common.model.plugin.PluginBindingResponse;
import ir.daneshrefah.scm.common.model.plugin.PluginBindingUpdateRequest;
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
    public List<PluginBindingResponse> bindPlugin(PluginBindingRequest request) {
        return pluginResolverService.getPluginBindingsByDefinitionId(request);
    }

    @JavaService(operationCode = OperationCode.SVC_PLUGIN_BINDING_CREATE)
    public PluginBindingResponse createPlugin(PluginBindingCreateRequest request) {
        return pluginResolverService.createPluginBinding(request);
    }

    @JavaService(operationCode = OperationCode.SVC_PLUGIN_BINDING_UPDATE)
    public PluginBindingResponse updatePluginBinding(PluginBindingUpdateRequest request){
        return pluginResolverService.updatePluginBinding(request);
    }
}
