package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAccount extends BaseModel<Long> {
    private Customer customer;
    private Account account;
    private CustomerRelationType relationType;
}
