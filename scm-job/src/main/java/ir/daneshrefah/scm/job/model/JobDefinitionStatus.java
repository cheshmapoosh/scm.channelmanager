package ir.daneshrefah.scm.job.model;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-23
 */
@RequiredArgsConstructor
public enum JobDefinitionStatus {

    INACTIVE(0), ACTIVE(1), IN_PROGRESS(2), FINISHED(3);

    private final int code;

    public static JobDefinitionStatus findByCode(int code) {
        return Arrays.stream(JobDefinitionStatus.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElse(null);
    }
}
