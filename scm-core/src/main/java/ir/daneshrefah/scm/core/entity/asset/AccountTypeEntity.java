package ir.daneshrefah.scm.core.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

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
@Table(name = "ACCOUNT_TYPE")
public class AccountTypeEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "ACCOUNT_TYPE_ID")
    private Long id;
    private String name;

}
