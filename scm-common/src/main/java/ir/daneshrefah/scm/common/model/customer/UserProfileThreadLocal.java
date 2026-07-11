package ir.daneshrefah.scm.common.model.customer;

import java.util.Optional;

public final class UserProfileThreadLocal {

    private static final ThreadLocal<UserProfile> USER_PROFILE = new ThreadLocal<>();

    private UserProfileThreadLocal() {
    }

    public static Optional<UserProfile> get() {
        return Optional.ofNullable(USER_PROFILE.get());
    }

    public static void set(UserProfile userProfile) {
        USER_PROFILE.set(userProfile);
    }

    public static void clear() {
        USER_PROFILE.remove();
    }
}
