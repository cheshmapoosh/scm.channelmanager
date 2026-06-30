package ir.daneshrefah.scm.uaa.web.login;

public record LoginViewModel(
        String clientId,
        String clientTitle,
        LoginTheme theme,
        boolean error,
        boolean stepTwoRequired
) {
}
