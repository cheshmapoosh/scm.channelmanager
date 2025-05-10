package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.Model;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ChannelServiceAccess}
 */
@Getter
@Setter
public class ChannelServiceAccess extends Model<Long> {
    @NotNull
    private Channel channel;
    @NotNull
    private Service service;
    private Integer fixedValue;
    private Integer ratedValue;
    private Boolean withdrawalAmount;
    @NotNull
    private Boolean active = false;
}