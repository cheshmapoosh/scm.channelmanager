package ir.daneshrefah.scm.core.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
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
@Table(name = "ACCOUNT")
public class AccountEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "ACCOUNT_ID")
    private Long id;
    private String accountNo;
    @ManyToOne
    @JoinColumn(name = "ACCOUNT_TYPE_ID")
    private AccountTypeEntity accountType;

}
