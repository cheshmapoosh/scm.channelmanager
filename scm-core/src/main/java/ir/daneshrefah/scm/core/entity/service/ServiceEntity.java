package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.core.converter.ServiceImplementationTypeConverter;
import ir.daneshrefah.scm.core.converter.ServiceStatusConverter;
import ir.daneshrefah.scm.core.converter.ServiceTypeConverter;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_SERVICE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "SERVICE_IMPLEMENTATION_TYPE_CODE", discriminatorType = DiscriminatorType.INTEGER)
public abstract class ServiceEntity extends AbstractDefaultEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SERVICE_ID")
    private String id;
    private String code;
    private String title;
    private String alias;
    private Integer version;
    private Boolean isSystemic;
    private String metadata;
    @Column(name = "SERVICE_TYPE_CODE")
    @Convert(converter = ServiceTypeConverter.class)
    private ServiceType type;
    @Convert(converter = ServiceStatusConverter.class)
    private ServiceStatus status;
    @Column(name = "SERVICE_IMPLEMENTATION_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceImplementationTypeConverter.class)
    private ServiceImplementationType implementationType;
    @Column(name = "REQUEST_JSON_SCHEMA", nullable = true)
    private String requestJsonSchema;
    @Column(name = "RESPONSE_JSON_SCHEMA", nullable = true)
    private String responseJsonSchema;
    private Boolean checkAccessFirstAuthentication;
    private Boolean checkAccessSecondAuthentication;
    private Boolean checkAccessService;
    private Boolean checkAccessAsset;
    @Column(name = "PROPERTY_NAME_AMOUNT")
    private String amountProperty;
    @Column(name = "PROPERTY_NAME_ASSET")
    private String assetProperty;
    @ManyToOne
    @JoinColumn(name = "PARENT_SERVICE_ID")
    private ServiceEntity parent;

}
