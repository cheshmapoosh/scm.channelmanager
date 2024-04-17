package ir.daneshrefah.scm.common.model.terminal;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class LegacyTerminal implements Serializable {

    private Long id;
    private Long parentId;
    private Boolean active;
    private Boolean published;
    private Long maxWithdrawalPerDay;
    private Long maxPersWithdrawalPerDay;
    private String name;
    private String code;

}
