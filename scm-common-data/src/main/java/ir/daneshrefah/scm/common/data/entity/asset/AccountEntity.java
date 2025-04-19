package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-03-25
 */
@Getter
@Setter
@Entity
@Table(name = "ACCOUNT")
public class AccountEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "ACCOUNT_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",sequenceName = "SQACCOUNT",allocationSize = 1)
    private Long id;
    private String accountNo;
    @ManyToOne
    @JoinColumn(name = "ACCOUNT_TYPE_ID")
    private AccountTypeEntity accountType;
    @ManyToOne
    @JoinColumn(name = "CORE_BANKING_SYSTEM_ID")
    private AssetProviderEntity assetProvider;
    @Column(name = "CLOSE")
    private Integer close;
    @Column(name = "CLOSE_DATE")
    private LocalDateTime closeDate;
    @Column(name = "REASON_CLOSE")
    private Integer reasonClose;

}
