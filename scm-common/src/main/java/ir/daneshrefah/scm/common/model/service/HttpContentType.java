package ir.daneshrefah.scm.common.model.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-14
 */
@Getter
@RequiredArgsConstructor
public enum HttpContentType {

    NONE(null),
    FORM_DATA("multipart/form-data"),
    X_WWW_FORM_URLENCODED("application/x-www-form-urlencoded"),
    BINARY("application/octet-stream"),
    GRAPHQL("application/json"),
    RAW_TEXT("text/plain"),
    RAW_JSON("application/json"),
    RAW_XML("application/xhtml+xml"),
    RAW_HTML("text/html");

    private final String value;

}
