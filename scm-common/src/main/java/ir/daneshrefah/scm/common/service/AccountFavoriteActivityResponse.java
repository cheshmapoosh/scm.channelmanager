package ir.daneshrefah.scm.common.service;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AccountFavoriteActivityResponse {
    private String accountNo;
    private Boolean isFavorite;
}
