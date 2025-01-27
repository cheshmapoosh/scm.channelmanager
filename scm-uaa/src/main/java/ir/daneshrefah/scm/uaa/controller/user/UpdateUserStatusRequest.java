package ir.daneshrefah.scm.uaa.controller.user;

import lombok.Data;

@Data
public class UpdateUserStatusRequest extends UserByNationalCodeFindRequest {
    private Boolean status;
}
