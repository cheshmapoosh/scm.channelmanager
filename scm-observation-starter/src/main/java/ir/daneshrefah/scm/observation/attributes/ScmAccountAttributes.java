package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmAccountAttributes {
    private ScmAccountAttributes() {
    }

    public static final ObservationAttributeKey<String> NO = ObservationAttributeKey.stringKey("scm.account.no", ObservationAttributePresence.EVENT_OPTIONAL, "Account number");
}
