package ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication;

import ir.daneshrefah.scm.uaa.config.PwaAuthenticationConfigProperties;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
class PasswordChangeWarningService {

    private final PwaAuthenticationConfigProperties properties;
    private final UserRepository userRepository;

    boolean shouldWarn(UserEntity user) {
        LocalDate currentDate = LocalDate.now();
        clearAbortPassIfExpired(user, currentDate);
        return shouldWarn(user.getLastDateOfFirstPasswordChange(),
                user.getLastReactionDateToFirstPasswordChange(), user.isAbortPass(), user.getLastEditDate(),
                user.getCreateDate(), currentDate);
    }

    boolean shouldWarn(LocalDate lastPasswordChangeDate, LocalDate lastReactionDate, boolean abortPass,
                       LocalDateTime lastEditDate,
                       LocalDateTime createDate, LocalDate currentDate) {
        Optional<LocalDate> passwordReferenceDate = resolvePasswordReferenceDate(
                lastPasswordChangeDate, lastReactionDate, abortPass, lastEditDate, createDate);
        if (passwordReferenceDate.isEmpty()) {
            return true;
        }

        Optional<Integer> warningPeriodDays = findWarningPeriodDays();
        if (warningPeriodDays.isEmpty()) {
            return true;
        }

        long daysSincePasswordChange = ChronoUnit.DAYS.between(passwordReferenceDate.get(), currentDate);
        return daysSincePasswordChange >= warningPeriodDays.get();
    }

    Optional<LocalDate> resolvePasswordReferenceDate(LocalDate lastPasswordChangeDate,
                                                     LocalDate lastReactionDate,
                                                     boolean abortPass,
                                                     LocalDateTime lastEditDate,
                                                     LocalDateTime createDate) {
        LocalDate referenceDate;
        if (lastPasswordChangeDate != null) {
            referenceDate = lastPasswordChangeDate;
        } else if (lastEditDate != null) {
            referenceDate = lastEditDate.toLocalDate();
        } else {
            referenceDate = createDate != null ? createDate.toLocalDate() : null;
        }

        if (abortPass && lastReactionDate != null
                && (referenceDate == null || lastReactionDate.isAfter(referenceDate))) {
            referenceDate = lastReactionDate;
        }
        return Optional.ofNullable(referenceDate);
    }

    private void clearAbortPassIfExpired(UserEntity user, LocalDate currentDate) {
        if (!user.isAbortPass() || user.getLastReactionDateToFirstPasswordChange() == null) {
            return;
        }

        Optional<Integer> warningPeriodDays = findWarningPeriodDays();
        if (warningPeriodDays.isEmpty()) {
            return;
        }

        long daysSinceReaction = ChronoUnit.DAYS.between(
                user.getLastReactionDateToFirstPasswordChange(), currentDate);
        if (daysSinceReaction >= warningPeriodDays.get()) {
            user.setAbortPass(false);
            userRepository.save(user);
        }
    }

    private Optional<Integer> findWarningPeriodDays() {
        PwaAuthenticationConfigProperties.LoginConfig loginConfig = properties.getLogin();
        Integer warningPeriodDays = loginConfig != null ? loginConfig.tokenChangeWarnPeriodDays() : null;
        if (warningPeriodDays == null || warningPeriodDays <= 0) {
            log.warn("Password change warning period is missing or invalid; password change warning will be enabled");
            return Optional.empty();
        }
        return Optional.of(warningPeriodDays);
    }
}
