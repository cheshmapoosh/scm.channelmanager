package ir.daneshrefah.scm.task.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_PRC_PROCESS_INSTANCE_WATCHER")
public class ProcessInstanceWatcherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROCESS_INSTANCE_WATCHER_ID")
    private Long id;

    @Column(name = "USER_ID")
    private Integer userId;

    @ManyToOne
    @JoinColumn(name = "PROCESS_ID")
    private ProcessInstanceEntity processInstance;

    @Column(name = "CREATE_BY", nullable = false)
    private Integer createBy;

    @Column(name = "CREATE_AT", nullable = false)
    private Date createAt;
}
