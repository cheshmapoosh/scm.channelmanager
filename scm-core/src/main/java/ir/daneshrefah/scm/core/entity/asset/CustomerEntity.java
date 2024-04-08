package ir.daneshrefah.scm.core.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import ir.daneshrefah.scm.core.entity.service.ExternalServiceProviderEntity;
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
@Table(name = "CUSTOMER")
public class CustomerEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "CUSTOMER_ID")
    private Long id;
    private String customerNo;
    @ManyToOne
    @JoinColumn(name = "PROVIDER_ID")
    private ExternalServiceProviderEntity provider;

}
