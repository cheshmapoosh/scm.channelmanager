package ir.daneshrefah.scm.common.error.management;

import ir.daneshrefah.scm.common.model.error.Error;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class CamelErrorWrapperException extends RuntimeException {
    private final List<Error> errors;
}
