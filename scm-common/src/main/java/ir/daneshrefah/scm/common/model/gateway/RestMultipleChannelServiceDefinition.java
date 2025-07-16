package ir.daneshrefah.scm.common.model.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestMultipleChannelServiceDefinition extends ChannelServiceDefinition {

    private String contextPath;
    private List<String> definitionIdList;
    private List<RestChannelServiceDefinition> definitions;

    @Override
    public ChannelServiceDefinitionType getType() {
        return ChannelServiceDefinitionType.REST_MULTIPLE;
    }
}
