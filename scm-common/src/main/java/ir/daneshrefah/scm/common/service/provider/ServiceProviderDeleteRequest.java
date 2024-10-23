package ir.daneshrefah.scm.common.service.provider;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceProviderDeleteRequest implements RequestData {
    private LocalDateTime lastEditDate;
    private String serviceProviderId;
}
