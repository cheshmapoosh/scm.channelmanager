package ir.daneshrefah.scm.core.entity.service.composition;

import ir.daneshrefah.scm.common.data.entity.AbstractDefaultEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
@Data
@Entity
@Table(name = "TBL_SCM_SERVICE_RELATION")
public class ServiceRelationEntity extends AbstractDefaultEntity<Long> {

    @Id
    @Column(name = "SERVICE_RELATION_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "SOURCE_SERVICE_ID")
    private ServiceEntity sourceService;
    private Integer order;
    @ManyToOne
    @JoinColumn(name = "TARGET_SRV_ID")
    private ServiceEntity targetService;
    @ManyToOne
    @JoinColumn(name = "TARGET_SRV_COMMIT_ID")
    private ServiceEntity targetServiceCommit;
    @ManyToOne
    @JoinColumn(name = "TARGET_SRV_REVERSE_ID")
    private ServiceEntity targetServiceReverse;

}
