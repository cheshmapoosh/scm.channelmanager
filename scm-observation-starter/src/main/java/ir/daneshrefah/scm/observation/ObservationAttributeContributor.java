package ir.daneshrefah.scm.observation;

import java.util.Collection;

public interface ObservationAttributeContributor {
    Collection<ObservationAttributeKey<?>> attributes();
}
