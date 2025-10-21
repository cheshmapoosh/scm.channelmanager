package ir.daneshrefah.scm.uaa.domain.pwa;

import ir.daneshrefah.scm.uaa.common.constants.AuthStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
public class PwaLogin implements Serializable {
    private Long id;
    private String agent;
    private String phoneNumber;
    private String username;
    private String realUsername;
    private String deviceModel;
    private ZonedDateTime blockedTime;
    private AuthStatus status;
    private ZonedDateTime lastLogin;
    private String ip;
    private transient String channelCode;
}
