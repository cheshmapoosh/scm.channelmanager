package ir.daneshrefah.scm.common.data.entity.asset;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
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
@Getter
@Setter
@Entity
@Table(name = "CUSTOMER")
public class CustomerEntity extends AbstractEntity<Long> {

    @Id
    @Column(name = "CUSTOMER_ID")
    @SequenceGenerator(name = "seq",sequenceName = "REF.SQCUSTOMER" ,allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "seq")
    private Long id;
    private String customerNo;
//    @ManyToOne
//    @JoinColumn(name = "PROVIDER_ID")
//    private ExternalServiceProviderEntity provider;

}
