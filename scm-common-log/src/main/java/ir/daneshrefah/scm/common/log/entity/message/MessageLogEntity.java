package ir.daneshrefah.scm.common.log.entity.message;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "message_log",schema = "LOGGER")
@Entity
public class MessageLogEntity implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",allocationSize = 1)
    private Long id;


    @Column(name = "correlation_id")
    private String correlationId;
    @NotNull
    @Column(name = "username")
    private String username;
    @Column(name = "real_username")
    private String realUsername;
    @Column(name = "access_param")
    private String accessParam;
    @Column(name = "body")
    private String body;
    @Column(name = "service_Type")
    private String serviceType;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LogStatus status;
    @Column(name = "ip")
    private String ip;
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;
    @Column(name = "allocated_time")
    private long respAllocatedTime;
    @Column(name = "client_type")
    private String clientType;
    @Column(name = "amount")
    private Long amount;

}
