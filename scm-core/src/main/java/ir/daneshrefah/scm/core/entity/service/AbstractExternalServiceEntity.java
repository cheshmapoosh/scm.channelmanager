package ir.daneshrefah.scm.core.entity.service;

import ir.daneshrefah.scm.common.model.service.ExternalServiceRequestBodyType;
import ir.daneshrefah.scm.core.entity.service.parameter.ParameterEntity;
import ir.daneshrefah.scm.core.entity.service.parameter.ResponseConditionEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-06
 */
@Setter
@Getter
@Entity
public abstract class AbstractExternalServiceEntity<T extends AbstractExternalServiceProviderEntity> extends ServiceEntity {

    @ManyToOne(targetEntity = AbstractExternalServiceProviderEntity.class)
    @JoinColumn(name = "IMPLEMENTATION_SERVICE_PROVIDER_ID")
    private T serviceProvider;
    @Enumerated(EnumType.STRING)
    private ExternalServiceRequestBodyType requestBodyType;
    @Enumerated(EnumType.STRING)
    private ExternalServiceRequestBodyType responseBodyType;
    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(name = "TBL_SCM_PARAMETER_EXTERNAL_SERVICE_RELATION"
            ,joinColumns = @JoinColumn(name = "SERVICE_ID")
            ,inverseJoinColumns = @JoinColumn(name = "PARAMETER_ID"))
    private List<ParameterEntity> parameters;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "SERVICE_ID")
    private List<ResponseConditionEntity> responseConditions;

}
