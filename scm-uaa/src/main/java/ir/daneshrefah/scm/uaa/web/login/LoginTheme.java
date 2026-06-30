package ir.daneshrefah.scm.uaa.web.login;

public record LoginTheme(
        String name,
        String displayName,
        String cssClass,
        String cssPath
) {
    public static LoginTheme defaultTheme() {
        return new LoginTheme("default", "SCM", "theme-default", null);
    }
}
