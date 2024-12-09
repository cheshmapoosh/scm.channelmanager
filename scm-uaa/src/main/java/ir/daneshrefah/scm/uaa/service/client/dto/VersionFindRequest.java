package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VersionFindRequest extends PagedRequestData {

    private Long id;
    private String creator;
    private String lastEditor;
    private String version;
    private Boolean isForced;
    private String signature;
    private ClientVersionStatus status;
}
