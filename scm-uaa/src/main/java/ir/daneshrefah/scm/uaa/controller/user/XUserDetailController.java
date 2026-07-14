package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.controller.BaseController;
import ir.daneshrefah.scm.uaa.repository.authentication.UserChannelAuthentication;
import ir.daneshrefah.scm.uaa.repository.authentication.UserChannelAuthenticationRepository;
import ir.daneshrefah.scm.uaa.service.user.XUserDetailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/x-user-detail")
@RequiredArgsConstructor
@Slf4j
public class XUserDetailController extends BaseController {
    private final UserChannelAuthenticationRepository authenticationRepository;
    private final XUserDetailService xUserDetailService;

    @GetMapping("/remove")
    public void removeXUserDetail(@RequestParam Integer userId) {
        log.info("start Removing list x user details for userId={}", userId);
        List<UserChannelAuthentication> userChannelAuthentication = getNickName(userId);
        if (Objects.isNull(userChannelAuthentication)) {
            return;
        }
        for (UserChannelAuthentication authentication : userChannelAuthentication) {
            log.info("Removing x user details for userId={}, nickName={}", authentication.getId(),  authentication.getNickName());
            xUserDetailService.removeXUserByUsername(authentication.getNickName().trim());
        }
    }

    private List<UserChannelAuthentication> getNickName(Integer userId) {
        List<UserChannelAuthentication> userChannelAuthentication = authenticationRepository.findByUser_IdAndChannel_IdIsIn(userId, List.of(TerminalType.MB.getLegacyTerminalId().intValue(), TerminalType.NIB.getLegacyTerminalId().intValue()));
        if (Objects.isNull(userChannelAuthentication) ||  userChannelAuthentication.isEmpty()) {
            log.info("user channel authentication with userId {} does not exist", userId);
            return null;
//            throw new NoMatchRecordFoundException("user channel authentication with userId {} does not exist", userId + "");
        }
        return userChannelAuthentication;
    }
}
