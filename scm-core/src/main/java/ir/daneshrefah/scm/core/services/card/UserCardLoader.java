package ir.daneshrefah.scm.core.services.card;

import ir.daneshrefah.scm.common.model.customer.Card;
import ir.daneshrefah.scm.core.mapper.CardMapper;
import ir.daneshrefah.scm.core.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCardLoader {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public List<Card> loadByPersonId(Integer personId) {
        return emptyIfNull(cardMapper.toModels(cardRepository.findAllByPersonId(personId)));
    }

    public List<Card> loadByUsername(String username) {
        return emptyIfNull(cardMapper.toModels(cardRepository.findAllByPersonUsername(username)));
    }

    private List<Card> emptyIfNull(List<Card> cards) {
        return cards == null ? List.of() : List.copyOf(cards);
    }
}
