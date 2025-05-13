package ir.daneshrefah.scm.common.model.asset;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerAccount extends AbstractAuditableModel<Long> {
    private Customer customer;
    private Account account;
    private CustomerRelationType relationType;
}
