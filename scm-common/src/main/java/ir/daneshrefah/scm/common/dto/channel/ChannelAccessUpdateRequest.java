package ir.daneshrefah.scm.common.dto.channel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelAccessUpdateRequest {
    private Long id;
    private Integer fixedValue;
    private Integer ratedValue;
    private Long withdrawalAmount;
    private Boolean active = false;
}
