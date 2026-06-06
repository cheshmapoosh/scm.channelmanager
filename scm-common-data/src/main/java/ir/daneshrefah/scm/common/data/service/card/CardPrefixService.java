package ir.daneshrefah.scm.common.data.service.card;


import ir.daneshrefah.scm.common.data.dto.card.CardPrefixDto;
import ir.daneshrefah.scm.common.data.mapper.CardPrefixMapper;
import ir.daneshrefah.scm.common.data.repository.card.CardPrefixRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class CardPrefixService {
    private final CardPrefixRepository cardPrefixRepository;
    private final CardPrefixMapper cardPrefixMapper;

    public Optional<CardPrefixDto> findOne(Long id) {
        log.info("Request to get CardPrefix : {}", id);
        return cardPrefixRepository.findById(id)
                .map(cardPrefixMapper::toDto);
    }
}
