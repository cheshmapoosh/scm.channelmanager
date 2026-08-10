package ir.daneshrefah.scm.uaa.controller.token;

import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.domain.pwa.ActivationRequestDto;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.PwaOauthResponseMapper;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequest;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationRequestHeader;
import ir.daneshrefah.scm.uaa.service.activation.pwa.model.ActivationResponse;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation.PwaActivationService;
import ir.daneshrefah.scm.uaa.utils.RequestUtils;
import ir.daneshrefah.scm.utils.functional.safe.SafeProcess;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class ActivationController {

    private static final String AGENT = "Agent";
    private static final String CHANNEL = "Channel";
    private static final String UUID = "UUID";
    private static final String DEVICE_MODEL = "DeviceModel";
    private static final String OS_VERSION = "OSVersion";
    private static final String APP_VERSION = "AppVersion";
    private static final String SIGNATURE = "Signature";
    private static final String HASH_CODE = "HashCode";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private final PwaActivationService activationService;
    private final PwaOauthResponseMapper responseMapper;


    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<ActivationResponse> register(@RequestHeader(AGENT) String agent,
                                                       @RequestHeader(CHANNEL) String channel,
                                                       @RequestHeader(UUID) UUID uuid,
                                                       @RequestHeader(DEVICE_MODEL) String deviceModel,
                                                       @RequestHeader(OS_VERSION) String osVersion,
                                                       @RequestHeader(APP_VERSION) String appVersion,
                                                       @RequestHeader(SIGNATURE) String signature,
                                                       @RequestHeader(value = HASH_CODE, required = false) String hashCode,
                                                       @RequestHeader(value = X_FORWARDED_FOR ,required = false) String ip,
                                                       @RequestBody ActivationRequestDto params,
                                                       HttpServletRequest req) {
        return SafeProcess.<ActivationResponse>of().tryGet(() -> activationService.activationRequest(ActivationRequest
                .builder()
                .phoneNumber(params.getPhoneNumber())
                .username(params.getUsername())
                .requestHeaders(ActivationRequestHeader.builder()
                        .agent(agent)
                        .channel(channel)
                        .uuid(uuid)
                        .deviceModel(deviceModel)
                        .osVersion(osVersion)
                        .appVersion(appVersion)
                        .signature(signature)
                        .hashCode(hashCode)
                        .ip(RequestUtils.getOrDefaultRequestIp(ip,req)).build())
                .build())).recover(responseMapper::map).toOptional()
                .map(response -> ResponseEntity.status(response.getHttpCode()).body(response))
                .orElseThrow();
    }


    @PostMapping("/valid")
    @ResponseBody
    public ResponseEntity<ActivationResponse> validate(@RequestHeader(AGENT) String agent,
                                                       @RequestHeader(CHANNEL) String channel,
                                                       @RequestHeader(UUID) UUID uuid,
                                                       @RequestHeader(DEVICE_MODEL) String deviceModel,
                                                       @RequestHeader(OS_VERSION) String osVersion,
                                                       @RequestHeader(APP_VERSION) String appVersion,
                                                       @RequestHeader(value = HASH_CODE, required = false) String hashCode,
                                                       @RequestHeader(value = X_FORWARDED_FOR,required = false) String ip,
                                                       @RequestBody ActivationRequestDto params,
                                                       HttpServletRequest req,HttpServletResponse resp) {
        return SafeProcess.<ActivationResponse>of().tryGet(() -> {
                    ActivationResponse response = activationService.verificationRequest(ActivationRequest
                            .builder()
                            .phoneNumber(params.getPhoneNumber())
                            .username(params.getUsername())
                            .otpCode(params.getActivationCode())
                            .requestHeaders(ActivationRequestHeader.builder()
                                    .agent(agent)
                                    .channel(channel)
                                    .uuid(uuid)
                                    .deviceModel(deviceModel)
                                    .osVersion(osVersion)
                                    .appVersion(appVersion)
                                    .hashCode(hashCode)
                                    .ip(RequestUtils.getOrDefaultRequestIp(ip,req)).build())
                            .build());
                    List<Cookie> cookies = createActivationCookies(response);
                    RequestUtils.enrichResponse(resp, cookies);
                    return response;
                }).recover(responseMapper::map).toOptional()
                .map(response -> ResponseEntity.status(response.getHttpCode()).body(response))
                .orElseThrow();
    }

    @SneakyThrows
    private List<Cookie> createActivationCookies(ActivationResponse response) {

        return Optional.ofNullable(response.getData())
                .map(token->{
                    String registryToken = URLEncoder.encode(response.getData(), StandardCharsets.UTF_8);
                    Cookie cookie = new Cookie(  Constants.REGISTRY_TOKEN_HEADER, registryToken);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(true);
                    cookie.setPath("/");
                    cookie.setMaxAge(Integer.MAX_VALUE);
                    cookie.setAttribute("SameSite", "None");
                    return Collections.singletonList(cookie);
                }).orElse(Collections.emptyList());
    }


}
