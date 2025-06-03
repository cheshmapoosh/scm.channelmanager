package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.common.validation.TerminalCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ConsoleAccountFavoriteActivityRequest implements RequestData {
    @NotNull
    private PersonType personType;
    @NotNull
    @NotBlank
    private String nationalId;
    @NotBlankIfPresent
    private String subOrganizationId;
    @TerminalCode
    private String terminal;
    private List<String> accountNoList;
    @NotNull
    private Boolean isFavorite;
}
