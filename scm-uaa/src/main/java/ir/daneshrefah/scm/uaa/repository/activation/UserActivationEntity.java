package ir.daneshrefah.scm.uaa.repository.activation;

import ir.daneshrefah.scm.common.data.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-03
 */
@Getter
@Setter
@Entity
@Table(name = "CLIENT_INFO",schema = "MBUAA")
public class UserActivationEntity {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Integer id;
    private String username;
    @Column(name = "PHONE_NUMBER")
    private String accessParameter;
    @Column(name = "CHANNEL")
    private String terminalCode;
    @Column(name = "REGISTRY_TOKEN")
    private String activationCode;
    private boolean activated;
    private String agent;
    private String deviceModel;
    private String osVersion;
    private String clientId;//
    @Column(name = "TOKEN_SET_TIME")
    private LocalDateTime tokenSetTime;
    @Column(name = "LAST_USED")
    private LocalDateTime lastUsed;

}
