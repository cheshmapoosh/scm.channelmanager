package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class AccountFavoriteActivityRequest implements RequestData {
    private List<String> accountNoList;
    @NotNull
    private Boolean isFavorite;
}
