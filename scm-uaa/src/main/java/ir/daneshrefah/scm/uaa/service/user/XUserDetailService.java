package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class XUserDetailService {

    private static final String DELETE_FROM_X_USER_BY_USERNAME_AND_CHANNEL_CODE_QUERY = "DELETE FROM REF.XUSER_DETAIL WHERE USERNAME = ? AND CHANNEL_CODE = ?";
    private static final String DELETE_FROM_X_USER_BY_USERNAME_QUERY = "DELETE FROM REF.XUSER_DETAIL WHERE USERNAME = ?";

    private final JdbcTemplate jdbcTemplate;

    public void removeXUserByUsernameAndChannelCode(UserEntity userEntity, String channelCode) {
        String username = getUsername(userEntity);
        removeXUserByUsernameAndChannelCode(username, channelCode);
    }

    public void removeXUserByUsernameAndChannelCode(String username, String channelCode) {
        jdbcTemplate.update(DELETE_FROM_X_USER_BY_USERNAME_AND_CHANNEL_CODE_QUERY, username, channelCode);
    }

    public void removeXUserByUsername(UserEntity userEntity) {
        String username = getUsername(userEntity);
        removeXUserByUsername(username);
    }

    public void removeXUserByUsername(String username) {
        jdbcTemplate.update(DELETE_FROM_X_USER_BY_USERNAME_QUERY, username);
    }

    private String getUsername(UserEntity userEntity) {
        ValidationUtils.checkNull(userEntity,() -> new MissingRequiredInputException("user"));
        GeneralPersonEntity person = userEntity.getPerson();
        return person.getUsername();
    }
}