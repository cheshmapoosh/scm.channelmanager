package ir.daneshrefah.scm.core.utils;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RouteUtils {
    @Getter
    private static final RouteUtils instance = new RouteUtils();

    public String generateRouteUniqId(Operation operation) {
        if (Objects.isNull(operation) || StringUtils.isBlank(operation.getName())) {
            throw new IllegalArgumentException("operation name is null or empty");
        }
        return md5(operation.getName().trim().toLowerCase());
    }

    public String generateRouteUniqId(String operationCode) {
        if (StringUtils.isBlank(operationCode)) {
            throw new IllegalArgumentException("operationCode is null or empty");
        }
        return md5(operationCode.trim().toLowerCase());
    }

    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }


}
