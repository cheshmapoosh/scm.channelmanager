package ir.daneshrefah.scm.uaa.domain.pwa;

import ir.daneshrefah.scm.uaa.common.constants.AuthStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
public class Register implements Serializable {
    private Long id;
    private String agent;
    private String username;
    private String deviceModel;
    private ZonedDateTime blockedTime;
    private String phoneNumber;
    private String appVersion;
    private AuthStatus status;
    private ZonedDateTime lastRegister;
    private String ip;
}
