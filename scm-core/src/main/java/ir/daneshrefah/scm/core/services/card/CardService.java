package ir.daneshrefah.scm.core.services.card;

import ir.daneshrefah.scm.common.model.customer.Card;
import ir.daneshrefah.scm.common.model.customer.UserProfile;

import java.util.List;

public interface CardService {

    List<Card> findUserCards(UserProfile profile, String fallbackUsername);
}
