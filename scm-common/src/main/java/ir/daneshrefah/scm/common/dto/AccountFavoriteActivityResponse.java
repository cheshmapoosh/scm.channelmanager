package ir.daneshrefah.scm.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AccountFavoriteActivityResponse {
    private List<String> accountNoList;
    private Boolean isFavorite;
}
