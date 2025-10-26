package ir.daneshrefah.scm.uaa.repository.activation.domain;

import ir.daneshrefah.scm.uaa.common.constants.AuthStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Table(name = "login")
@Entity
@Getter
@Setter
public class PwaLoginEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",sequenceName = "SEQUENCE_GENERATOR")
    private Long id;

    @Column(name = "agent", nullable = false)
    private String agent;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "real_username")
    private String realUsername;

    @Column(name = "device_model", nullable = false)
    private String deviceModel;

    @Column(name = "blocked_time")
    private ZonedDateTime blockedTime;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AuthStatus status;

    @Column(name = "last_login")
    private ZonedDateTime lastLogin;

    @Column(name = "ip")
    private String ip;

}
