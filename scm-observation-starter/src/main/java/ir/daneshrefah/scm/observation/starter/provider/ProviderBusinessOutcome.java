package ir.daneshrefah.scm.observation.starter.provider;

public record ProviderBusinessOutcome(
        boolean success,
        String responseCode,
        String errorCode,
        String errorType
) {
    public static final String EXCHANGE_PROPERTY = "scm.provider.business.outcome";
    public static final String BUSINESS_ERROR_TYPE = "business";
    public static final String TECHNICAL_ERROR_TYPE = "technical";

    public ProviderBusinessOutcome {
        responseCode = textOrNull(responseCode);
        errorCode = textOrNull(errorCode);
        errorType = textOrNull(errorType);
    }

    public static ProviderBusinessOutcome success(String responseCode) {
        return new ProviderBusinessOutcome(true, responseCode, null, null);
    }

    public static ProviderBusinessOutcome businessFailure(String responseCode, String errorCode) {
        String safeErrorCode = firstText(errorCode, responseCode, BUSINESS_ERROR_TYPE);
        return new ProviderBusinessOutcome(false, responseCode, safeErrorCode, BUSINESS_ERROR_TYPE);
    }

    public static ProviderBusinessOutcome technicalFailure(String responseCode, Throwable failure) {
        return technicalFailure(responseCode, errorCode(failure), failure);
    }

    public static ProviderBusinessOutcome technicalFailure(String responseCode, String errorCode, Throwable failure) {
        return new ProviderBusinessOutcome(
                false,
                responseCode,
                firstText(errorCode, errorCode(failure), TECHNICAL_ERROR_TYPE),
                TECHNICAL_ERROR_TYPE
        );
    }

    public String eventOutcome() {
        return success ? "success" : "failure";
    }

    public String safeErrorCode() {
        return firstText(errorCode, responseCode, success ? null : errorType);
    }

    public boolean businessFailure() {
        return !success && BUSINESS_ERROR_TYPE.equals(errorType);
    }

    public boolean technicalFailure() {
        return !success && TECHNICAL_ERROR_TYPE.equals(errorType);
    }

    private static String errorCode(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current.getClass().getSimpleName().contains("Timeout")) {
                return "PROVIDER_TIMEOUT";
            }
            current = current.getCause();
        }
        return failure == null ? null : failure.getClass().getSimpleName();
    }

    private static String firstText(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            String text = textOrNull(value);
            if (text != null) {
                return text;
            }
        }
        return null;
    }

    private static String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
