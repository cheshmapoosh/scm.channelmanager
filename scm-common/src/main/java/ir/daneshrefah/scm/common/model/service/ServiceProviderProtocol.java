package ir.daneshrefah.scm.common.model.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-05
 */
@Getter
@RequiredArgsConstructor
public enum ServiceProviderProtocol {

    REST(1),
    SOAP(2),
    JMS(3),
    TCP(4),
    CUSTOM(20);

    private final Integer code;

    public static ServiceProviderProtocol findByCode(Integer code) {
        return Arrays.stream(ServiceProviderProtocol.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }

    public static ServiceProviderProtocol findByName(String  name) {
        return Arrays.stream(ServiceProviderProtocol.values())
                .filter(s -> s.name().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

}
