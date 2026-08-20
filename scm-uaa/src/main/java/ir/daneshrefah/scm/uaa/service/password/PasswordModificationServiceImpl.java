package ir.daneshrefah.scm.uaa.service.password;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PasswordModificationServiceImpl implements PasswordModificationService {

    private final UserService userService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PasswordAbortModificationResponse abortPasswordChangeWarning() {
        UserAuthentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (authentication == null || !authentication.isFullyAuthenticated()) {
            throw new AuthenticationRequiredException();
        }

        UserEntity user = userService.findUser(authentication);
        LocalDate reactionDate = LocalDate.now();
        user.setLastReactionDateToFirstPasswordChange(reactionDate);
        user.setAbortPass(true);
        userRepository.save(user);
        return new PasswordAbortModificationResponse(true, reactionDate);
    }
}
