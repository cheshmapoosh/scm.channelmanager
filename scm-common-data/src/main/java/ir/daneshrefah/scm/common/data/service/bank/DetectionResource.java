package ir.daneshrefah.scm.common.data.service.bank;

import ir.daneshrefah.scm.common.data.dto.bank.BankDto;
import ir.daneshrefah.scm.common.data.dto.card.CardPrefixDto;
import ir.daneshrefah.scm.common.data.dto.card.CardResult;
import ir.daneshrefah.scm.common.data.service.card.CardPrefixService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service("detectionResource")
@AllArgsConstructor
@Slf4j
public class DetectionResource {
    public final CardPrefixService cardPrefixService;
    public final BankService bankService;

    public JSONObject detectBankByCardNumber(JSONObject body) {
        body.forEach((key, cardNum) -> {
            Optional<BankDto> bank;
            CardResult cardResult = new CardResult();
            cardResult.setCard((String) cardNum);
            try {
                cardNum = StringUtils.trim((String) cardNum);
                String iin = StringUtils.substring((String) cardNum, 0, 6);
                Optional<CardPrefixDto> card = Optional.empty();
                if (StringUtils.isNumeric(iin)) {
                    Long prefix = Long.valueOf(iin);
                    card = cardPrefixService.findOne(prefix);
                }
                else{
                    log.warn("Invalid card value[len={}] '{}'", StringUtils.length((CharSequence) cardNum), cardNum);
                }
                if (card.isPresent()) {
                    bank = bankService.findOne(card.get().getBankId());
                    cardResult.setValid(true);

                    if (bank.isPresent())
                        cardResult.setBank(bank.get());
                    else {
                        BankDto bankDTO = new BankDto();
                        bankDTO.setBic("BUNWNXXXXX");
                        bankDTO.setName("---");
                        bankDTO.setIs_home_bank(false);
                        cardResult.setBank(bankDTO);
                        cardResult.setValid(true);
                    }
                }else {
                    BankDto bankDTO = new BankDto();
                    bankDTO.setBic("BUNWNXXXXX");
                    bankDTO.setName("---");
                    bankDTO.setIs_home_bank(false);
                    cardResult.setBank(bankDTO);
                    cardResult.setValid(true);
                }
            } catch (NumberFormatException e) {
                log.error("Could not detect iban", e);
                cardResult.setValid(false);
            }

            body.put(key, cardResult);
        });

        return body;
    }
}
