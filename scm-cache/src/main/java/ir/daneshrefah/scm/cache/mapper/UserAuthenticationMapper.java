package ir.daneshrefah.scm.cache.mapper;

import ir.daneshrefah.scm.cache.domain.dto.UserAuthenticationTO;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;


public interface UserAuthenticationMapper {
    UserAuthentication mapToUserAuthentication(UserAuthenticationTO userAuthenticationTO);

    UserAuthenticationTO mapUserAuthenticationTO(String json);
    UserAuthenticationTO mapUserAuthenticationTO(UserAuthentication userAuthentication);

    String generateKey(UserAuthenticationTO authenticationTO);

    String generateKey(String nickName, String terminalCode);
}
