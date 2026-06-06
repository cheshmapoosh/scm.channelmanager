package ir.daneshrefah.scm.common.data.dto.card;

import ir.daneshrefah.scm.common.data.dto.bank.BankDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardResult {
    private String card;
    private BankDto bank;
    private boolean isValid;
}
