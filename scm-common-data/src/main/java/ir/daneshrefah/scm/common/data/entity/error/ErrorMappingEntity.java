package ir.daneshrefah.scm.common.data.entity.error;

import ir.daneshrefah.scm.common.data.converter.MessageStatusTypeConverter;
import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

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
public class ErrorMappingEntity extends AbstractAuditableEntity<Long> {

    @Id
    @Column(name = "ERROR_MAPPING_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "EXT_SRV_PROVIDER_ID")
    private String providerId;
    private String providerErrorCode;
    @Column(name = "EXCEPTION_CLASS_NAME") //TODO COLUMN SHOULD CHANGES
    private String errorMessage;
    @Column(name = "EXCEPTION_OVERRIDE_NAME")
    private String exceptionOverrideName;
    private Integer scmErrorCode;
    @Column(name = "STATUS_CODE")
    @Convert(converter = MessageStatusTypeConverter.class)
    private MessageStatus status;

}
