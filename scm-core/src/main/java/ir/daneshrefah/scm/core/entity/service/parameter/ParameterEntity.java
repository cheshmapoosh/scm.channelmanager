package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.data.entity.AbstractStringAuditableEntity;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterActionType;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterType;
import ir.daneshrefah.scm.core.converter.ParameterActionTypeConverter;
import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_PARAMETERS")
public class ParameterEntity extends AbstractStringAuditableEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "PARAMETER_ID")
    private String id;
    private String name;
    @Embedded
    private ParameterDatasourceEntity datasource;
    @Enumerated(EnumType.STRING)
    private ParameterType type;
    private String tag;
    private boolean required;
    private Integer order;
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private ParameterEntity parent;
    @Convert(converter = ParameterActionTypeConverter.class)
    private ParameterActionType actionType;
    @Column(name = "DEFAULT_VALUE")
    private String defaultValue;
    @Column(name = "PARAMETER_TITLE")
    private String title;

    @ManyToOne
    @JoinTable(name = "TBL_SCM_PARAMETER_SERVICE_RELATION"
            ,joinColumns = @JoinColumn(name = "PARAMETER_ID")
            ,inverseJoinColumns = @JoinColumn(name = "SERVICE_ID"))
    private ServiceEntity service;

    @ManyToOne
    @JoinTable(name = "TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION"
            ,joinColumns = @JoinColumn(name = "PARAMETER_ID")
            ,inverseJoinColumns = @JoinColumn(name = "RESPONSE_CONDITION_ID"))
    private ResponseEntity responseCondition;

    @ManyToOne
    @JoinTable(name = "TBL_SCM_PARAMETER_SERVICE_PROVIDER_RELATION"
            , joinColumns = @JoinColumn(name = "PARAMETER_ID")
            , inverseJoinColumns = @JoinColumn(name = "SERVICE_PROVIDER_ID"))
    private AbstractExternalServiceProviderEntity serviceProvider;


}
