package ir.daneshrefah.scm.common.constant.otp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OtpDeviceType {

    GO3("GO3"),
    GEMALTO_LAVA("GEMALTO_LAVA"),
    MB("MB"),
    K3000("K3000"),
    SABAMB("sabamb");

    private final String value;
}
