package ir.daneshrefah.scm.core.entity.service.parameter;

import ir.daneshrefah.scm.common.data.entity.AbstractStringAuditableEntity;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.core.converter.ExternalServiceRequestBodyTypeConverter;
import ir.daneshrefah.scm.core.entity.transformer.TransformerEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Setter
@Getter
@Table(name = "TBL_SCM_SERVICE_RESPONSE_CONDITION")
@Entity
@DynamicUpdate
public class ResponseEntity extends AbstractStringAuditableEntity<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "RESP_CONDITION_ID")
    private String id;
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "RESPONSE_CONDITION_ID")
    private List<ParameterDatasourceConditionEntity> conditions;
    @ManyToOne
    @JoinColumn(name = "RESP_TRANSFORMER_ID")
    private TransformerEntity responseTransformer;
    @Column(name = "RESP_ERROR_CODE")
    private String responseErrorCodeProperty;
    @Column(name = "RESP_ERROR_MESSAGE")
    private String responseErrorMessageProperty;
    @OneToMany(fetch = FetchType.EAGER,orphanRemoval = true)
    @JoinTable(name = "TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION"
    ,joinColumns = @JoinColumn(name = "RESPONSE_CONDITION_ID")
    ,inverseJoinColumns = @JoinColumn(name = "PARAMETER_ID"))
    private List<ParameterEntity> responseParameters;
    @Convert(converter = ExternalServiceRequestBodyTypeConverter.class)
    @Column(name = "RESP_BODY_TYPE")
    private ExternalServiceBodyType responseBodyType;
    @Column(name = "STATUS")
    private boolean enable;
    @Column(name = "RESP_TITLE")
    private String title;
}
