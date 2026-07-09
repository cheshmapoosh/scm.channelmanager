package ir.daneshrefah.scm.uaa.controller.activation;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.config.ratelimit.RateLimitBuckets;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.annotation.WithRateLimit;
import ir.daneshrefah.scm.uaa.service.activation.nib.UserActivationMessagePublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ir.daneshrefah.scm.common.model.message.MessageStatus.SC_SUCCESS;

@RequiredArgsConstructor
@RestController
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequestMapping("/api/activation")
@CrossOrigin
public class UserActivationController {

    private final UserActivationMessagePublisherService userPreActivationService;

    @GetMapping("/active")
    @PreAuthorize("hasAuthority(@grant.scopes.ACTIVATION)")
    @WithRateLimit(name = RateLimitBuckets.UAA_NIB_ACTIVATION, perUser = true)
    public ResponseEntity<?> findPagedClientList() {
        Authentication authentication = AuthenticationUtils.getAuthentication();
        UserAuthentication.AuthenticationDetail details = (UserAuthentication.AuthenticationDetail) authentication.getDetails();
        Jwt jwt = (Jwt) details.getLoginData();
        userPreActivationService.publish(authentication.getName(), jwt.getClaimAsString(Constants.CLAIM_KEY_ACTIVATOR_TERMINAL_CODE));
        return ResponseEntity.status(HttpStatus.OK).body(Message.builder().status(SC_SUCCESS).build());
    }
}
