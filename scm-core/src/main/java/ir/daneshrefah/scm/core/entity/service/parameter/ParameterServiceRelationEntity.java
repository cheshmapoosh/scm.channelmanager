package ir.daneshrefah.scm.core.entity.service.parameter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_PARAMETER_EXTERNAL_SERVICE_RELATION")
@Getter
@Setter
public class ParameterServiceRelationEntity {

    @Id
    @Column(name = "PARAMETER_ID")
    private String parameterId;
    @Column(name = "SERVICE_ID")
    private String serviceId;

}
