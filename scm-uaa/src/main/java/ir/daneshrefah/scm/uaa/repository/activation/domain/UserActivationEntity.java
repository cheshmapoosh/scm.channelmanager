package ir.daneshrefah.scm.uaa.repository.activation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;


@Entity
@Table(name = "client_info",schema = "MBUAA")
@Getter
@Setter
public class UserActivationEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",sequenceName = "SEQUENCE_GENERATOR")
    private Long id;

    @Column(name = "agent", nullable = false, length = 20)
    private String agent;

    @Column(name = "os_version", length = 20)
    private String osVersion;

    @Column(name = "last_used", nullable = false)
    private ZonedDateTime lastUsed;

    @Column(name = "activated", nullable = false)
    private Boolean activated;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "device_model")
    private String deviceModel;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "activation_code", length = 10)
    private String activationCode;

    @Column(name = "token_set_time")
    private ZonedDateTime tokenSetTime;

    @Column(name = "code_valid")
    private Boolean codeValid;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "registry_token")
    private String registryToken;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private DeviceClientEntity deviceClient;

}
