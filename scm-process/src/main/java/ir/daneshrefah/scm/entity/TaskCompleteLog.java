package ir.daneshrefah.scm.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SCM_TASK_COMPLETE_LOG")
public class TaskCompleteLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COMPLETE_AT", nullable = false)
    private Date completeAt;

    @Column(name = "TASK_ID")
    private String taskId;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ISSUER_USERNAME")
    private String issuerUserName;

    @Column(name = "EXCEPTION_CLASS_NAME")
    private String exceptionClassName;

    @Column(name = "PAYLOAD")
    @Lob
    private String payload;
}