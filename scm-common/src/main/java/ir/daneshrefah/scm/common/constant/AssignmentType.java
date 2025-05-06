package ir.daneshrefah.scm.common.constant;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AssignmentType {
    REVOKE,ASSIGN;

    public static AssignmentType getAssignmentType(final String value) {
        return Arrays.stream(values())
                .filter(a->a.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(()-> new InvalidInputException("AssignmentType"));
    }
}
