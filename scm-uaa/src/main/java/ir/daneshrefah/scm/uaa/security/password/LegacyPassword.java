package ir.daneshrefah.scm.uaa.security.password;

/**
 * Carries the raw credential plus the legacy username salt input required by the old SCM MD5 hash.
 * The raw value is intentionally not exposed through {@link #toString()}.
 */
public final class LegacyPassword implements CharSequence {
    private final CharSequence password;
    private final String username;

    public LegacyPassword(CharSequence password, String username) {
        this.password = password == null ? "" : password;
        this.username = username;
    }

    public String username() {
        return username;
    }

    public CharSequence password() {
        return password;
    }

    @Override
    public int length() {
        return password.length();
    }

    @Override
    public char charAt(int index) {
        return password.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return password.subSequence(start, end);
    }

    @Override
    public String toString() {
        return "[PROTECTED]";
    }
}
