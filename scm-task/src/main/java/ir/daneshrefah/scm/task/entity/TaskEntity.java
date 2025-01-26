package ir.daneshrefah.scm.task.entity;

import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.task.converter.TaskStatusConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "TBL_PRC_TASKS")
public class TaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TASK_ID")
    private Long id;
    @Column(name = "USER_ID")
    private Integer userId;
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "PROCESS_ID")
    private ProcessInstanceEntity processInstance;
    @OneToMany(mappedBy = "taskEntity", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TaskLogEntity> taskLogEntities;
    @Column(name = "FULL_NAME")
    private String fullName;
    @Column(name = "STATUS")
    @Convert(converter = TaskStatusConverter.class)
    private TaskStatusEnum taskStatus;
    @Column(name = "IS_GLOBAL")
    private boolean global;
    @Column(name = "IS_SIGNER")
    private Boolean signer;
    @Column(name = "ARCHIVE_NO", updatable = false)
    private Long archiveNo;
    @Column(name = "CREATE_BY")
    private Integer createdBy;
    @Column(name = "CREATE_AT")
    private Date createAt;
    @Column(name = "UPDATE_AT")
    private Date updateAt;
}
