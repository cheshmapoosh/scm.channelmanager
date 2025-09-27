package ir.daneshrefah.scm.common.service.plugin;

import ir.daneshrefah.scm.common.dto.plugin.PluginBindingCreateRequest;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingRequest;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingResponse;
import ir.daneshrefah.scm.common.dto.plugin.PluginBindingUpdateRequest;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginHandlerModel;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;

import java.util.List;

public interface PluginResolverService {

    List<PluginDetail> resolveOrderedPluginDetails(Channel channel);

    List<PluginDetail> resolveOrderedPluginDetails(List<PluginDetail> channelPluginDetails, Service service, PluginPhase phase);

    List<PluginDetail> resolveOrderedPluginDetails(Operation operation, PluginPhase phase);

    List<PluginBindingResponse> getPluginBindingsByDefinitionId(PluginBindingRequest request);

    PluginBindingResponse createPluginBinding(PluginBindingCreateRequest request);

    PluginBindingResponse updatePluginBinding(PluginBindingUpdateRequest request);

    List<PluginHandlerModel> getPluginHandlerModels();
}
