package ir.daneshrefah.scm.common.model.terminal;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LegacyTerminal extends BaseModel<Integer> {
    private String name;
    private Integer id;
    private Integer parentId;
    private String code;
    private BigDecimal maxPersWithdrawalPerDay;
    private BigDecimal maxWithdrawalPerDay;
    private Boolean published;
    private Boolean active;
}


