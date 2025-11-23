package ir.daneshrefah.scm.common.dto.channelServiceDefination;

import ir.daneshrefah.scm.common.dto.definition.DefinitionRequest;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChannelServiceDefinitionCreateRequest {
    private Long channelServiceAccessId;
    private ChannelServiceDefinitionType type;
    private String gatewayId;
    private List<String> operationNames;
    private String contextPath;
    private DefinitionRequest definition;
}
