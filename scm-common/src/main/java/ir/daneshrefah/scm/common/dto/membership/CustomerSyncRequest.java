package ir.daneshrefah.scm.common.dto.membership;

import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerSyncRequest extends CustomerFindRequest{
    @NotNull
    @Numeric
    private String assetProviderId;
    private List<String> accountNumberList;
}
