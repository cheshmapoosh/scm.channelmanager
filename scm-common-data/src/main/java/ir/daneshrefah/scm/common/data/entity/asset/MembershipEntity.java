package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
//import ir.daneshrefah.scm.common.model.customer.AssetType;
//import ir.daneshrefah.scm.core.converter.AssetTypeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Entity
@Table(name = "MEMBERSHIP")
//@DiscriminatorColumn(name = "ASSET_TYPE", discriminatorType = DiscriminatorType.INTEGER)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Getter
@Setter
public class MembershipEntity extends AbstractEntity<Long> {

    @Column(name = "MEMBERSHIP_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "NICK_NAME")
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private GeneralPersonEntity person;

    @Column(name = "DEFAULT_ACCOUNT")
    private Boolean defaultAccount;

    @Column(name = "ARCHIVE_NO")
    private Integer archiveNumber;

    @OneToOne
    @JoinColumn(name = "CUSTOMER_ACCOUNT_ID")
    private CustomerAccountEntity customerAccount;

    @Column(name = "CLOSE")
    private Boolean close;

}
