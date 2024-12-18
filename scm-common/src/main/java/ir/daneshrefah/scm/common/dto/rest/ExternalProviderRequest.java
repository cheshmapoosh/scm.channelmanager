package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExternalProviderRequest {
    @NotNull
    private ServiceProviderProtocol protocol;
}
