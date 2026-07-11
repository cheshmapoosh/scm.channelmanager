package ir.daneshrefah.scm.core.services.card;

import ir.daneshrefah.scm.common.model.customer.Card;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.core.entity.person.CardEntity;
import ir.daneshrefah.scm.core.mapper.CardMapper;
import ir.daneshrefah.scm.core.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    public List<Card> findUserCards(UserProfile profile, String fallbackUsername) {
        List<CardEntity> cardEntities = profile.getPersonId() != null
                ? cardRepository.findAllByPersonId(profile.getPersonId())
                : cardRepository.findAllByPersonUsername(StringUtils.defaultIfBlank(profile.getPersonUsername(), fallbackUsername));
        return cardMapper.toModels(cardEntities);
    }
}
