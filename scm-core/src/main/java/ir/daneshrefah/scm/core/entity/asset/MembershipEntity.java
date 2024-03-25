package ir.daneshrefah.scm.core.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.model.customer.AssetType;
import ir.daneshrefah.scm.core.converter.AssetTypeConverter;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Data
@Entity
@Table(name = "CUSTOMERACCOUNT")
@SecondaryTable(name = "MEMBERSHIP")
public class MembershipEntity extends AbstractEntity<Long> {

    @Column(name = "MEMBERSHIP_ID", table = "MEMBERSHIP")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Id
    @Column(name = "CUSTOMER_ACCOUNT_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerAccountId;
    @Column(name = "NICK_NAME", table = "MEMBERSHIP")
    private String nickname;
    @Column(name = "DEFAULT_ACCOUNT", table = "MEMBERSHIP")
    private Boolean defaultAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", table = "MEMBERSHIP")
    private GeneralPersonEntity person;
    @ManyToOne
    @JoinColumn(name = "CUSTOMER_ID")
    private CustomerEntity customer;
    @Convert(converter = AssetTypeConverter.class)
    private AssetType assetType;
    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID")
    private AccountEntity account;

}
