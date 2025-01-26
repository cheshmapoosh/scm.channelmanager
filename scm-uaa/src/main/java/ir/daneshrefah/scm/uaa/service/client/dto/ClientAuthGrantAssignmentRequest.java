package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClientAuthGrantAssignmentRequest implements RequestData {
    @NotNull
    private AuthorizationGrantType grantType;
    @NotNull
    @Numeric
    private Long clientId;

}
