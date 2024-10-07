package ir.daneshrefah.scm.core.entity.service.parameter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION")
@Setter
@Getter
public class ParameterResponseConditionRelationEntity {
    @Id
    @Column(name = "PARAMETER_ID")
    private String parameterId;
    @Column(name = "RESPONSE_CONDITION_ID")
    private String responseConditionId;
}
