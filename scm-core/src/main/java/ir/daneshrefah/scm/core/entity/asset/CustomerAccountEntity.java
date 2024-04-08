package ir.daneshrefah.scm.core.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "CUSTOMERACCOUNT")
public class CustomerAccountEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "CUSTOMER_ACCOUNT_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private AccountEntity account;

    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private CustomerEntity customer;
}
