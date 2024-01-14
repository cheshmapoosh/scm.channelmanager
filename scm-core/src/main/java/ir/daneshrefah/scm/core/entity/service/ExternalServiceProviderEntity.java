package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
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
 * @since 2023-12-26
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_SERVICE_PROVIDER")
public class ExternalServiceProviderEntity extends AbstractDefaultEntity<String> {
    @Id
    @Column(name = "SERVICE_PROVIDER_ID")
    private String id;
    private String code;
    private String title;
    private String providerClassName;
    private String metadata;
    private boolean customerProvided;
    private String customerProviderClassName;
//    @Column(name = "CUSTOMER_PROVIDE_METHOD_CODE"/*, insertable = false, updatable = false*/)
//    @Convert(converter = CustomerProvideMethodConverter.class)
//    private CustomerProvideMethod customerProvideMethod;

}
