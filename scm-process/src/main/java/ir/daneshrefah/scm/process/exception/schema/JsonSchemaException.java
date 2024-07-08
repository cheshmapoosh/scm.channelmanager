package ir.daneshrefah.scm.process.exception.schema;

import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class JsonSchemaException extends AbstractProcessException {

    public JsonSchemaException(String source, String message) {
        super(source, message);
    }

    public JsonSchemaException(String source, String message, Throwable cause) {
        super(source, message, cause);
    }
}
