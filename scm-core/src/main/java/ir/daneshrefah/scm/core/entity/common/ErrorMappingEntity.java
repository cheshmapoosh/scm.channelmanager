package ir.daneshrefah.scm.core.entity.common;

import ir.daneshrefah.scm.core.converter.MessageStatusTypeConverter;
import ir.daneshrefah.scm.core.entity.AbstractEntity;
import ir.daneshrefah.scm.core.entity.component.ServiceComponentProviderEntity;
import ir.daneshrefah.scm.plugin.api.model.message.Status;
import jakarta.persistence.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-30
 */
@Entity
@Table(name = "TBL_SCM_ERROR_MAPPING")
public class ErrorMappingEntity extends AbstractEntity<String> {

    @Id
    @Column(name = "ERROR_MAPPING_ID")
    private String id;

    @ManyToOne
    @JoinColumn(name = "SERVICE_COMPONENT_PROVIDER_ID")
    private ServiceComponentProviderEntity serviceComponentProviderEntity;
    private String providerErrorCode;
    private String scmErrorCode;
    @Column(name = "STATUS_CODE")
    @Convert(converter = MessageStatusTypeConverter.class)
    private Status status;
    private String message;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ServiceComponentProviderEntity getServiceComponentProviderEntity() {
        return serviceComponentProviderEntity;
    }

    public void setServiceComponentProviderEntity(ServiceComponentProviderEntity serviceComponentProviderEntity) {
        this.serviceComponentProviderEntity = serviceComponentProviderEntity;
    }

    public String getProviderErrorCode() {
        return providerErrorCode;
    }

    public void setProviderErrorCode(String providerErrorCode) {
        this.providerErrorCode = providerErrorCode;
    }

    public String getScmErrorCode() {
        return scmErrorCode;
    }

    public void setScmErrorCode(String scmErrorCode) {
        this.scmErrorCode = scmErrorCode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
