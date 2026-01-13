package ir.daneshrefah.scm.common.token;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenBankingToken {

    private String access_token;
    private int expires_in;
    private String token_type;
    private String scope;

}
