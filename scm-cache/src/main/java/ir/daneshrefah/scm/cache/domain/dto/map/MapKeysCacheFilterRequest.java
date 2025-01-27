package ir.daneshrefah.scm.cache.domain.dto.map;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MapKeysCacheFilterRequest extends PagedRequestData {
    private String name;
}
