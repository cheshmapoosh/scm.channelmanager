package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.AbstractModel;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.model.gateway.Service;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ChannelServiceAccess extends AbstractModel<Long> {
    @NotNull
    private Channel channel;
    @NotNull
    private Service service;
    private Integer fixedValue;
    private Integer ratedValue;
    private Long withdrawalAmount;
    @NotNull
    private Boolean active = false;
}
