package ir.daneshrefah.scm.uaa.domain.pwa;

import ir.daneshrefah.scm.uaa.common.constants.AppVersionStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DeviceClient implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String application;
    private String appVersion;
    private String signature;
    private String url;
    private AppVersionStatus status;
}
