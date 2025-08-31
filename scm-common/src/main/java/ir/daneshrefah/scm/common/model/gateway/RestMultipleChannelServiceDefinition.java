package ir.daneshrefah.scm.common.model.gateway;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class RestMultipleChannelServiceDefinition extends BaseChannelServiceDefinition {

    private String contextPath;
    private List<MultiRouteDetail> multiRouteDetails;

    @Override
    public ChannelServiceDefinitionType getType() {
        return ChannelServiceDefinitionType.REST_MULTIPLE;
    }

    @Getter
    @Setter
    public static class MultiRouteDetail{
        private String definitionId;
        private String operationCode;
        private RestChannelServiceDefinition definition;
    }
}
