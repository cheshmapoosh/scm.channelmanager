package ir.daneshrefah.scm.common.dto.asset;

import ir.daneshrefah.scm.common.model.gateway.CmChannel;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class ChannelServiceAccess implements Serializable {
    private Long id;
    private CmChannel channel;
    private EbService ebService;
    private Integer fixedValue;
    private Integer ratedValue;
    private String withdrawalAmount;
    private Boolean active;

}
