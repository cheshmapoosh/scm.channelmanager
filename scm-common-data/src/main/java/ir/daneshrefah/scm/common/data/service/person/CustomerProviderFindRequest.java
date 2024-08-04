package ir.daneshrefah.scm.common.data.service.person;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CustomerProviderFindRequest extends CustomerFindRequest {
    private String assetProviderId;
}
