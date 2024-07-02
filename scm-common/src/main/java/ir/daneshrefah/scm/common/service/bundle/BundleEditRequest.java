package ir.daneshrefah.scm.common.service.bundle;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BundleEditRequest implements RequestData {
    private Long id;
    private String value;
    private LocalDateTime lastEditDate;
}
