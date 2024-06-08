package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExceptionResolverLevel {
    DEFAULT(1),
    ALL_BASE_EXCEPTION(2),
    ALL_RUN_TIME_EXCEPTION(3),
    ALL_EXCEPTION(4),
    ALL_THROWABLE(5);
    private final int order;
}
