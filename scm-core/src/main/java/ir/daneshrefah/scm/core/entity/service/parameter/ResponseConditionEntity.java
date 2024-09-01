package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.data.entity.AbstractVersionAbleDefaultEntity;
import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Table(name = "TBL_SCM_SERVICE_RESPONSE_CONDITION")
@Entity
public class ResponseConditionEntity extends AbstractVersionAbleDefaultEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "RESPONSE_CONDITION_ID")
    private List<ParameterDatasourceConditionEntity> conditions;
    @ManyToOne
    @JoinColumn(name = "RESPONSE_TRANSFORMER_ID")
    private TransformerEntity responseTransformer;
    @Column(name = "RESP_ERROR_CODE")
    private String responseExceptionErrorCodeProperty;
    @Column(name = "RESP_ERROR_MESSAGE")
    private String responseExceptionErrorMessageProperty;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION"
    ,joinColumns = @JoinColumn(name = "RESPONSE_CONDITION_ID")
    ,inverseJoinColumns = @JoinColumn(name = "PARAMETER_ID"))
    private List<ParameterEntity> responseParameters;
}
