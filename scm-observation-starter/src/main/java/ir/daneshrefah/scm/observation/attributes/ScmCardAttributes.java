package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmCardAttributes {
    private ScmCardAttributes() {
    }

    public static final ObservationAttributeKey<String> NO = ObservationAttributeKey.stringKey("scm.card.no", ObservationAttributePresence.EVENT_OPTIONAL, "Card number");
}
