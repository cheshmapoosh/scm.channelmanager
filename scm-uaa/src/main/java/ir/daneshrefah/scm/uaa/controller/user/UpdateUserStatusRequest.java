package ir.daneshrefah.scm.uaa.controller.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserStatusRequest extends UserByNationalCodeFindRequest {
    private Boolean status;
}
