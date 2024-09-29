package ir.daneshrefah.scm.task.entity;

import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.task.converter.TaskStatusConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_PRC_TASKS_LOG")
public class TaskLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TASK_LOG_ID")
    private Long id;

    @Column(name = "LAST_CHANNEL_CODE")
    private String lastChannelCode;

    @ManyToOne
    @JoinColumn(name = "TASK_ID")
    private TaskEntity taskEntity;

    @Column(name = "STATUS")
    @Convert(converter = TaskStatusConverter.class)
    private TaskStatusEnum status;

    @Column(name = "CREATE_BY")
    private Integer createdBy;

    @Column(name = "CREATE_AT")
    private Date createAt;
}
