package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.BaseModel;
import lombok.Data;

@Data
public class CustomerAccount extends BaseModel<Long> {
    private Customer customer;
    private Account account;
}
