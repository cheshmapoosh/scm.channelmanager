package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.core.converter.CustomerProvideMethodConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.plugin.api.model.service.external.CustomerProvideMethod;
import jakarta.persistence.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
@Entity
@Table(name = "TBL_SCM_SERVICE_PROVIDER")
public class ExternalServiceProviderEntity extends AbstractEntity<String> {
    @Id
    @Column(name = "SERVICE_PROVIDER_ID")
    private String id;
    private String code;
    private String title;
    private String providerClassName;
    private String metadata;
    @Column(name = "CUSTOMER_PROVIDE_METHOD_CODE"/*, insertable = false, updatable = false*/)
    @Convert(converter = CustomerProvideMethodConverter.class)
    private CustomerProvideMethod customerProvideMethod;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProviderClassName() {
        return providerClassName;
    }

    public void setProviderClassName(String componentClassName) {
        this.providerClassName = componentClassName;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public CustomerProvideMethod getCustomerProvideMethod() {
        return customerProvideMethod;
    }

    public void setCustomerProvideMethod(CustomerProvideMethod customerProvideMethod) {
        this.customerProvideMethod = customerProvideMethod;
    }
}
