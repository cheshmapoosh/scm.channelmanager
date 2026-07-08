package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.controller.BaseController;
import ir.daneshrefah.scm.uaa.repository.authentication.UserChannelAuthentication;
import ir.daneshrefah.scm.uaa.repository.authentication.UserChannelAuthenticationRepository;
import ir.daneshrefah.scm.uaa.service.user.XUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/x-user-detail")
@RequiredArgsConstructor
public class XUserDetailController extends BaseController {
    private final UserChannelAuthenticationRepository authenticationRepository;
    private final XUserDetailService xUserDetailService;

    @GetMapping("/remove")
    public void removeXUserDetail(Integer userId) {
        List<UserChannelAuthentication> userChannelAuthentication = getNickName(userId);
        for (UserChannelAuthentication authentication : userChannelAuthentication) {
            xUserDetailService.removeXUserByUsername(authentication.getNickName());
        }
    }

    private List<UserChannelAuthentication> getNickName(Integer userId) {
        List<UserChannelAuthentication> userChannelAuthentication = authenticationRepository.findByUser_IdAndChannel_IdIsIn(userId, List.of(TerminalType.MB.getLegacyTerminalId(), TerminalType.NIB.getLegacyTerminalId()));
        if (Objects.isNull(userChannelAuthentication) ||  userChannelAuthentication.isEmpty()) {
            LOGGER.info("user channel authentication with userId {} does not exist", userId);
            throw new NoMatchRecordFoundException("user channel authentication with userId {} does not exist", userId + "");
        }
        return userChannelAuthentication;
    }
}
