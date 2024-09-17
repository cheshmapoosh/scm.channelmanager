package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccountFavoriteActivityRequest implements RequestData {
    private String accountNo;
    private Boolean isFavorite;
}
