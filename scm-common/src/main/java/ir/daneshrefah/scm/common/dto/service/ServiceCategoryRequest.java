package ir.daneshrefah.scm.common.dto.service;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceCategoryRequest extends PagedRequestData {
    private Short id;
    private String name;
    private String description;
}
