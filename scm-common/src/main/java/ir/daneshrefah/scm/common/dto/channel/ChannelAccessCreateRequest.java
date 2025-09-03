package ir.daneshrefah.scm.common.dto.channel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelAccessCreateRequest {
    private Long id;
    private Short channelId;
    private Short serviceId;
    private Integer fixedValue;
    private Integer ratedValue;
    private Long withdrawalAmount;
    private Boolean active = false;
}
