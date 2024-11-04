package ir.daneshrefah.scm.common.dto.bundle;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BundleEditRequest implements RequestData {
    private Long id;
    private String value;
    private LocalDateTime lastEditDate;
}
