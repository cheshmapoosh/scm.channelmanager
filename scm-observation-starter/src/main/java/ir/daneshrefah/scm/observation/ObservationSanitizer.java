package ir.daneshrefah.scm.observation;

public interface ObservationSanitizer {
    Object sanitize(String fieldName, Object value);
}
