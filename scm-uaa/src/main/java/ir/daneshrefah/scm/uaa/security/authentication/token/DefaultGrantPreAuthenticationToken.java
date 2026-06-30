package ir.daneshrefah.scm.uaa.security.authentication.token;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
@ToString(callSuper = true, exclude = {"accessParam", "otpCode", "registryToken"})
public class DefaultGrantPreAuthenticationToken implements Serializable {
    private String accessParam;
    private String channel;
    private String agent;
    private String deviceModel;
    private String appVersion;
    private String signature;
    private String ip;
    private String otpCode;
    private String registryToken;
    private String uuid;
    private String hashcode;
    private String terminalType;
    private String operationSystemVersion;


}
