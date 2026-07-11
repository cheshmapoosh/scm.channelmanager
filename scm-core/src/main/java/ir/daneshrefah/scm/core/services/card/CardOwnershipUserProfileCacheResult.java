package ir.daneshrefah.scm.core.services.card;

import ir.daneshrefah.scm.common.model.customer.UserProfile;

public record CardOwnershipUserProfileCacheResult(
        UserProfile userProfile,
        boolean validated
) {
}
