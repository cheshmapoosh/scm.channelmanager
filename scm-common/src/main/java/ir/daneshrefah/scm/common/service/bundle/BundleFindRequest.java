package ir.daneshrefah.scm.common.service.bundle;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Data;

@Data
public class BundleFindRequest extends PagedRequestData {
    private String locale;
    private String key;
    private String value;
}
