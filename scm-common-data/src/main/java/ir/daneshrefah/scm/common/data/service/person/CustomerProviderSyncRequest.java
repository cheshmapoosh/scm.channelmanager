package ir.daneshrefah.scm.common.data.service.person;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerProviderSyncRequest extends CustomerFindRequest{
    private String assetProviderId;
    private Boolean syncAll;
    private List<String> membershipSyncIdList;
    private List<String> accountSyncIdList;
}
