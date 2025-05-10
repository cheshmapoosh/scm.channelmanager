package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.AuditableModel;
import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAccount extends AuditableModel<Long> {
    private Customer customer;
    private Account account;
    private CustomerRelationType relationType;
}
