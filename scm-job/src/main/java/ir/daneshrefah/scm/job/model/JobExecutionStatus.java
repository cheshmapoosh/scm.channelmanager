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
public enum JobExecutionStatus {

    IN_PROGRESS(0), SUCCESS(1), ERROR(2);

    private final int code;

    public static JobExecutionStatus findByCode(int code) {
        return Arrays.stream(JobExecutionStatus.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElse(null);
    }
}
