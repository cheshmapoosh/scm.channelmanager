package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmCardAttributes {
    private ScmCardAttributes() {
    }

    public static final ObservationAttributeKey<String> NO = ObservationAttributeKey.stringKey("scm.card.no", "Card number");
}
