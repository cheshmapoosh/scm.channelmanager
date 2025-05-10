package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.model.service.ServiceType;
import ir.daneshrefah.scm.core.converter.ServiceImplementationTypeConverter;
import ir.daneshrefah.scm.core.converter.ServiceStatusConverter;
import ir.daneshrefah.scm.core.converter.ServiceTypeConverter;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_SERVICE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "SERVICE_IMPL_TYPE_CODE", discriminatorType = DiscriminatorType.INTEGER)
public abstract class ServiceEntity extends AbstractAuditableEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SERVICE_ID")
    private String id;
    private String code;
    private String title;
    private String alias;
    private Integer version;
    @Column(insertable = false, updatable = false)
    private Boolean isSystemic;
    @Column(name = "SERVICE_TYPE_CODE")
    @Convert(converter = ServiceTypeConverter.class)
    private ServiceType type;
    @Convert(converter = ServiceStatusConverter.class)
    private ServiceStatus status;
    @Column(name = "SERVICE_IMPL_TYPE_CODE", insertable = false, updatable = false)
    @Convert(converter = ServiceImplementationTypeConverter.class)
    private ServiceImplementationType implementationType;
    @Column(name = "REQUEST_JSON_SCHEMA", nullable = true)
    private String requestJsonSchema;
    @Column(name = "RESPONSE_JSON_SCHEMA", nullable = true)
    private String responseJsonSchema;
    @Column(name = "CHECK_ACCESS_FIRST_AUTH")
    private Boolean checkAccessFirstAuthentication;
    @Column(name = "CHECK_ACCESS_SECOND_AUTH")
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
    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinTable(name = "TBL_SCM_PARAMETER_SERVICE_RELATION"
            ,joinColumns = @JoinColumn(name = "SERVICE_ID")
            ,inverseJoinColumns = @JoinColumn(name = "PARAMETER_ID"))
    private List<ParameterEntity> parameters;

}
