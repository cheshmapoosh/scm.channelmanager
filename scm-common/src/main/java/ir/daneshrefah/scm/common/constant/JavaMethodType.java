package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JavaMethodType {

    NULL(0),
    REPORT(1),
    FINANCE(2),
    INQUIRY(3),
    PARENT(4),
    ENTITY_CREATE(5),
    ENTITY_UPDATE(6),
    ENTITY_DELETE(7);

    private final Integer code;

}
