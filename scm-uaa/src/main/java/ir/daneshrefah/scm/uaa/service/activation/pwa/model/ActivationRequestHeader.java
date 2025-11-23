package ir.daneshrefah.scm.uaa.service.activation.pwa.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Setter
@Accessors(chain = true)
@Builder
public class ActivationRequestHeader {

    private String agent;
    private String channel;
    private UUID uuid;
    private String deviceModel;
    private String osVersion;
    private String appVersion;
    private String signature;
    private String hashCode;
    private String ip;

}
