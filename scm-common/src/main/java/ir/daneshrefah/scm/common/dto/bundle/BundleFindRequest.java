package ir.daneshrefah.scm.common.dto.bundle;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Data;

@Data
public class BundleFindRequest extends PagedRequestData {
    private String locale;
    private String key;
    private String value;
}
