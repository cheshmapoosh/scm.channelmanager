package ir.daneshrefah.scm.core.entity.common;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.core.converter.MessageStatusTypeConverter;
import ir.daneshrefah.scm.core.entity.service.ExternalServiceProviderEntity;
import ir.daneshrefah.scm.common.model.message.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-30
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_ERROR_MAPPING")
public class ErrorMappingEntity extends AbstractDefaultEntity<String> {

    @Id
    @Column(name = "ERROR_MAPPING_ID")
    private String id;

    @ManyToOne
    @JoinColumn(name = "SERVICE_PROVIDER_ID")
    private ExternalServiceProviderEntity provider;
    private String providerErrorCode;
    private String scmErrorCode;
    @Column(name = "STATUS_CODE")
    @Convert(converter = MessageStatusTypeConverter.class)
    private Status status;
    private String message;

}
