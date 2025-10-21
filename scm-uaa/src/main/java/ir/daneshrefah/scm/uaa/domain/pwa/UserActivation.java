package ir.daneshrefah.scm.uaa.domain.pwa;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
public class UserActivation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String agent;
    private String osVersion;
    private ZonedDateTime lastUsed;
    private Boolean activated;
    private String phoneNumber;
    private String channel;
    private String username;
    private String deviceModel;
    private String uuid;
    private String fcmToken;
    private String activationCode;
    private ZonedDateTime tokenSetTime;
    private Boolean codeValid;
    private Integer retryCount;
    private String registryToken;
    private DeviceClient deviceClient;

}
