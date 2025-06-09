package ir.daneshrefah.scm.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

import java.util.List;

@Setter
@Getter
@Builder
public class AccountFavoriteActivityResponse {
    @Singular("accountNoList")
    private List<String> accountNoList;
    private Boolean isFavorite;
}
