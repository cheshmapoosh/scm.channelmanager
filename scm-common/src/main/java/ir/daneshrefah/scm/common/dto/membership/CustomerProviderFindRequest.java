package ir.daneshrefah.scm.common.dto.membership;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerProviderFindRequest extends CustomerFindRequest {
    private String assetProviderId;
}
