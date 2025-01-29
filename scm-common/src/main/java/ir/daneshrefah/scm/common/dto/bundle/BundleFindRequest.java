package ir.daneshrefah.scm.common.dto.bundle;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BundleFindRequest extends PagedRequestData {
    private String locale;
    private String key;
    private String value;
}
