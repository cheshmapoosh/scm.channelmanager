package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmAccountAttributes {
    private ScmAccountAttributes() {
    }

    public static final ObservationAttributeKey<String> NO = ObservationAttributeKey.stringKey("scm.account.no", "Account number");
}
