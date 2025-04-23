package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.uaa.controller.BaseController;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.service.otp.verify.UserOtpVerifyService;
import ir.daneshrefah.scm.uaa.service.user.OtpUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController extends BaseController {

    private final OtpUserService otpUserService;
    private final UserOtpVerifyService userOtpVerifyService;

    /**
     * Sends an OTP SMS to the currently logged-in user.
     */
  //  @PreAuthorize("isFullyAuthenticated()")
    @PostMapping("/sms-by-logged-in-user")
    @CrossOrigin
    public OtpSendResponse sendOtpSms(@RequestBody SmsOtpSendRequest request) {
        return otpUserService.sendOtpByLoggedInUser(request);
    }

    /**
     * Sends an OTP SMS to a delegated user
     */
  //  @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/sms-by-delegated-user")
    public OtpSendResponse sendDelegatedOtpSms(@RequestBody DelegatedSmsOtpSendRequest request) {
        return otpUserService.sendOtpByDelegated(request);
    }

    /**
     * Sends OTP TO User By Username
     */
   // @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/sms-by-username")
    public OtpSendResponse sendOtpSmsByUsername(@RequestBody OtpSmsBasedUsernameRequest request) {
        return otpUserService.sendOtpByUsername(request);
    }

    /**
     * Sends OTP TO User By Nickname
     */
   // @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/sms-by-nickname")
    public OtpSendResponse sendOtpSmsByNickname(@RequestBody OtpSmsBasedNicknameRequest request) {
        return otpUserService.sendOtpByNickname(request);
    }

    /**
     * Send OTP TO anonymous By Address
     */
   // @PreAuthorize("isAnonymous()")
    @GetMapping("/public/sms-authentication/{recipient}")
    public OtpSendResponse sendAuthenticationOtpSms(@PathVariable("recipient") String recipientAddress) {
        return otpUserService.sendOtpByAddress(recipientAddress);
    }

  //  @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/verify-by-logged-in-user")
    public OtpVerifyResponse verifyOtpByLoggedInUser(@RequestBody VerifyOtpByLoggedInUserRequest request) {
        return userOtpVerifyService.verifyOtpByLoggedInUser(request);
    }

   // @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/verify-by-delegated")
    public OtpVerifyResponse verifyOtpByDelegatedUser(@RequestBody VerifyOtpByDelegatedUserRequest request) {
        return userOtpVerifyService.verifyOtpByDelegatedUser(request);
    }

   // @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/verify-by-username")
    public OtpVerifyResponse verifyOtpByUsername(@RequestBody VerifyOtpByUsernameRequest request) {
        return userOtpVerifyService.verifyOtpByUsername(request);
    }

   // @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/verify-by-nickname")
    public OtpVerifyResponse verifyOtpNickname(@RequestBody VerifyOtpByNicknameRequest request) {
        return userOtpVerifyService.verifyOtpByNickname(request);
    }

  //  @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/verify-by-national-code")
    public OtpVerifyResponse verifyOtpNationalCode(@RequestBody VerifyOtpByNationalCodeRequest request) {
        return userOtpVerifyService.verifyOtpByNationalCode(request);
    }
}
