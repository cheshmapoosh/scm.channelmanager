package ir.daneshrefah.scm.uaa.repository.activation.domain;

import ir.daneshrefah.scm.uaa.common.constants.AppVersionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;


@Entity
@Table(name = "client", schema = "MBUAA")
@Getter
@Setter
public class DeviceClientEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator",sequenceName = "SEQUENCE_GENERATOR")
    private Long id;

    @Column(name = "APPLICATION")
    private String application;

    @Column(name = "app_version", nullable = false, length = 12)
    private String appVersion;

    @Column(name = "signature", nullable = false)
    private String signature;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AppVersionStatus status;

}
