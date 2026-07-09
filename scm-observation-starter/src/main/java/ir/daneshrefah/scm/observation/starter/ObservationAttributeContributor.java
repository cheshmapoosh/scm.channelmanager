package ir.daneshrefah.scm.observation.starter;

import java.util.Collection;

public interface ObservationAttributeContributor {
    Collection<ObservationAttributeKey<?>> attributes();
}
