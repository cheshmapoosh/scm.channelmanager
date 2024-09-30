package ir.daneshrefah.scm.common.service;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AccountFavoriteActivityResponse {
    private List<String> accountNoList;
    private Boolean isFavorite;
}
