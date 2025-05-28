package ir.daneshrefah.scm.core.entity.condition;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.core.entity.operation.OperationEntity;
import ir.daneshrefah.scm.core.entity.service.ScmServiceEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_SCM_SERVICE_CONDITION")
@Setter
@Getter
public class ServiceConditionEntity extends ConditionBaseEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SERVICE_CONDITION_ID")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "SERVICE_ID")
    private ScmServiceEntity service;

}
