package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
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
        removeXUserByUsernameAndChannelCode(userEntity.getNickname(), channelCode);
    }

    /**
     * Removes a user from the cache based on their username and channel code.
     * <p>
     * Note: The username corresponds to the 'nickName' field in the USER_CHANNEL_AUTHENTICATION table.
     * </p>
     *
     * @param username     The nickname of the user in the USER_CHANNEL_AUTHENTICATION table.
     * @param channelCode
     */
    public void removeXUserByUsernameAndChannelCode(String username, String channelCode) {
        jdbcTemplate.update(DELETE_FROM_X_USER_BY_USERNAME_AND_CHANNEL_CODE_QUERY, username, channelCode);
    }

    /**
     * Removes a user from the XUSER_DETAIL table based on their username.
     * <p>
     * Note: The username corresponds to the 'nickName' field in the USER_CHANNEL_AUTHENTICATION table.
     * </p>
     *
     * @param username     The nickname of the user in the USER_CHANNEL_AUTHENTICATION table.
     */
    public void removeXUserByUsername(String username) {
        jdbcTemplate.update(DELETE_FROM_X_USER_BY_USERNAME_QUERY, username);
    }
}