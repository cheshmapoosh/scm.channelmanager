package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.common.plugin.PluginAware;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestChannelServiceDefinition extends ChannelServiceDefinition {

    private HttpMethod httpMethod;
    private String path;
    private List<String> pluginChains;
    private List<PluginAware> pluginAwares;

    @Override
    public ChannelServiceDefinitionType getType() {
        return ChannelServiceDefinitionType.REST;
    }


}
