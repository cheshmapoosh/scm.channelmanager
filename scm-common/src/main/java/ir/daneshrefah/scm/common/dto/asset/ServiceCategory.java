package ir.daneshrefah.scm.common.dto.asset;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ServiceCategory {
    private Integer serviceCategoryId;
    private String name;
    private String description;
}
