package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Data;

/*
 * TODO applyTransactionAuthentication
 * */
@Data
public class AuthenticationMethodModificationRequest implements RequestData {

       private AuthenticationMethod authenticationMethod;
       private String credential;


//       /*
//        * TODO send OTP service must be handle selected authentication method
//        * */
}
