package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {
    ACTIVE(true),
    DE_ACTIVE(false),
    /**
     * Use this value for getting status from database, by using another status
     * system automatically ignored database status.
     */
    DEFAULT(null);

    private final Boolean booleanValue;

}
