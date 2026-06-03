package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.model.service.HttpMethod;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InboundChannelServiceDefinition extends BaseChannelServiceDefinition {

    private HttpMethod method;
    private String path;
    @Override
    public ChannelServiceDefinitionType getType() {
        return ChannelServiceDefinitionType.INBOUND;
    }


}
