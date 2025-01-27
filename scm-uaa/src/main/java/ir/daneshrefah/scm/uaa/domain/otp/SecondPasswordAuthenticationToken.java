package ir.daneshrefah.scm.uaa.domain.otp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SecondPasswordAuthenticationToken {

    private Object principal;
    private Object credentials;
}