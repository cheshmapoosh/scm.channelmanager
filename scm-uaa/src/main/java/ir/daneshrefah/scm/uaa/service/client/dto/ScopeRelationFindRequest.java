package ir.daneshrefah.scm.uaa.service.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScopeRelationFindRequest extends PagedRequestData {
    @Schema(description = "client numeric instance id")
    private Long clientId;
}
