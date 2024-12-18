package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScopeFindRequest extends PagedRequestData {

    private Long id;
    private String creator;
    private String lastEditor;
    private String code;
    private String title;
}
