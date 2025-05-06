package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JavaMethodType {
    /**
     * Set NULL status for accept database status, by selecting another status
     * system automatically ignore database status
     */
    NULL(0),
    REPORT(1), // LIKE POST METHOD ON HTTP
    FINANCE(2),
    INQUIRY(3),// LIKE GET METHOD ON HTTP
    PARENT(4),
    ENTITY_CREATE(5),
    ENTITY_UPDATE(6),
    ENTITY_DELETE(7);

    private final Integer code;

}
