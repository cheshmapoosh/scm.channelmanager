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
public enum JobImplementationType {

    SERVICE(1), JAVA(2), STORE_PROCEDURE(3), HTTP(4);

    private final int code;

    public static JobImplementationType findByCode(int code) {
        return Arrays.stream(JobImplementationType.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElse(null);
    }
}
