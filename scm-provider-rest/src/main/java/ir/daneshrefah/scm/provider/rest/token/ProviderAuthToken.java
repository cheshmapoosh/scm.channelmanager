package ir.daneshrefah.scm.provider.rest.token;

public record ProviderAuthToken(
        String accessToken,
        String tokenType
) {
}
