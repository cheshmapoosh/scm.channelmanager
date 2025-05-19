package ir.daneshrefah.scm.task.entity;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.converter.ProcessStatusConverter;
import ir.daneshrefah.scm.task.converter.TransactionDataConverter;
import ir.daneshrefah.scm.task.converter.ProcessCodeConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "TBL_PRC_PROCESS_INSTANCE")
public class ProcessInstanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROCESS_ID")
    private Long id;

    @Column(name = "CONFIRM_USER_ID")
    private Integer confirmUserId;

    @Column(name = "ACCOUNT_NO", nullable = false)
    private String accountNo;

    @Column(name = "TRANSACTION_DATA", nullable = false)
    @Convert(converter = TransactionDataConverter.class)
    private JsonNode transactionData;

    @Column(name = "LAST_MESSAGE_SEQUENCE_ID")
    private String lastMessageSequenceId;

    @Column(name = "CORRELATION_ID")
    private String correlationId;

    @Column(name = "PROCESS_CODE", nullable = false)
    @Convert(converter = ProcessCodeConverter.class)
    private ProcessCodeEnum processCode;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "DESTINATION")
    private String destination;
    @OneToMany(mappedBy = "processInstance", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TaskEntity> tasks = new ArrayList<>();

    @OneToMany(mappedBy = "processInstance", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ProcessInstanceWatcherEntity> watcherEntities = new ArrayList<>();

    @Column(name = "STATUS", nullable = false)
    @Convert(converter = ProcessStatusConverter.class)
    private ProcessStatusEnum processStatus;

    @Column(name = "ARCHIVE_NO", updatable = false)
    private Long archiveNo;

    @Column(name = "CREATE_BY", nullable = false)
    private Integer createBy;

    @Column(name = "UPDATE_BY")
    private Integer updateBy;

    @Column(name = "CREATE_AT", nullable = false)
    private Date createAt;

    @Column(name = "UPDATE_AT")
    private Date updateAt;

    public void addTaskEntity(TaskEntity task) {
        this.tasks.add(task);
        task.setProcessInstance(this);
    }
    public void addTaskEntities(List<TaskEntity> tasks) {
        this.tasks.addAll(tasks);
        this.tasks.forEach(task -> task.setProcessInstance(this));
    }
}
