package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ServiceBucket {

    UUA_ACTIVATION_SERVICE("uaa_nib_activation");

    private final String bucketName;

}

