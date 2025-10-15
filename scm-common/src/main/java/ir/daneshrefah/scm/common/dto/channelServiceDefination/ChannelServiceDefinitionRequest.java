package ir.daneshrefah.scm.common.dto.channelServiceDefination;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelServiceDefinitionRequest extends PagedRequestData {
    private Short serviceId;
    private Short channelId;
}
