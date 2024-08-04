package ir.daneshrefah.scm.core.entity.asset;

import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.core.converter.CustomerRelationTypeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CUSTOMERACCOUNT")
public class CustomerAccountEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "CUSTOMER_ACCOUNT_ID")
    @SequenceGenerator(name = "customer_account_seq",sequenceName = "REF.SQCUSTOMERACCOUNT",allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "customer_account_seq")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private AccountEntity account;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private CustomerEntity customer;

    @Convert(converter = CustomerRelationTypeConverter.class)
    private CustomerRelationType relationType;
}
