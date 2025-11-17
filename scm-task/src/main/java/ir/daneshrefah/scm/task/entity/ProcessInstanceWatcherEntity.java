package ir.daneshrefah.scm.task.entity;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.task.converter.ProcessWatcherTypeConverter;
import ir.daneshrefah.scm.task.converter.TransactionDataConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TBL_PRC_PROCESS_INSTANCE_WATCHER", uniqueConstraints = {
        @UniqueConstraint(name = "UK_WATCHER_ROW_TYPE_PROCESS", columnNames = {"ROW_NO", "TYPE", "PROCESS_ID"})
})
public class ProcessInstanceWatcherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PROCESS_INSTANCE_WATCHER_ID")
    private Long id;

    @Column(name = "ROW_NO")
    private Integer rowNo;

    @Column(name = "USER_ID")
    private Integer userId;

    @Convert(converter = ProcessWatcherTypeConverter.class)
    @Column(name = "TYPE")
    private ProcessWatcherEnum type;

    @Convert(converter = TransactionDataConverter.class)
    @Column(name = "DATA")
    private JsonNode data;

    @ManyToOne
    @JoinColumn(name = "PROCESS_ID")
    private ProcessInstanceEntity processInstance;

    @Column(name = "ARCHIVE_NO", updatable = false)
    private Long archiveNo;

    @Column(name = "CREATE_BY", nullable = false)
    private Integer createBy;

    @Column(name = "CREATE_AT", nullable = false)
    private Date createAt;
}
