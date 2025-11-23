package ir.daneshrefah.scm.common.dto.channel;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChannelAccessUpdateRequest {
    private Long id;
    @NotNull
    private Integer fixedValue;
    @NotNull
    private Integer ratedValue;
    private Long withdrawalAmount;
    @NotNull
    private Boolean active;
}
