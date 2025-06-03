package ir.daneshrefah.scm.common.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ChangeDefaultAccountStatusResponse {
    private String defaultAccountNumber;
}
