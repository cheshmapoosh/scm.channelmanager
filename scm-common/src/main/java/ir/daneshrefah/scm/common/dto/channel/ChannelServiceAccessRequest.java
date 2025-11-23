package ir.daneshrefah.scm.common.dto.channel;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelServiceAccessRequest extends PagedRequestData {
    private Long serviceId;
}
