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
@Table(name = "SCM_PROCESS_START_LOG", schema = "REF")
public class ProcessStartLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "START_AT")
    private Date startAt;

    @Column(name = "PROCESS_KEY")
    private String processKey;

    @Column(name = "PROCESS_ID")
    private String processId;

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