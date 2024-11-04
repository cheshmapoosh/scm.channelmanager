package ir.daneshrefah.scm.common.dto.provider;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceProviderDeleteRequest implements RequestData {
    private LocalDateTime lastEditDate;
    private String serviceProviderId;
}
