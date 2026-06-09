package ir.daneshrefah.scm.uaa.service.shahkar;

import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarInquiryRequest;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarInquiryResponse;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarStatus;
import ir.daneshrefah.scm.uaa.service.shahkar.token.ShahkarTokenProvider;
import ir.daneshrefah.scm.uaa.service.shahkar.transport.ShahkarApiClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Component
public class ShahkarOwnershipService {

    private static final Logger log = LoggerFactory.getLogger(ShahkarOwnershipService.class);

    private final ShahkarTokenProvider tokenProvider;
    private final ShahkarApiClient apiClient;

    /** Optional: run blocking calls on virtual threads */
    private final ExecutorService shahkarVirtualThreadExecutor;

    public ShahkarStatus checkOwnership(String nationalId, String mobile) {
        try {
            Future<ShahkarStatus> f = shahkarVirtualThreadExecutor.submit(() -> doCheck(nationalId, mobile));
            return f.get();
        } catch (Exception e) {
            log.warn("Shahkar ownership check failed (FAIL). nationalId={}, mobile={}", mask(nationalId), maskMobile(mobile), e);
            return ShahkarStatus.FAIL;
        }
    }

    private ShahkarStatus doCheck(String nationalId, String mobileNo) {
        try {
            String token = tokenProvider.getValidAccessToken();

            ShahkarInquiryRequest req = new ShahkarInquiryRequest(nationalId, mobileNo,"0");
            ShahkarInquiryResponse resp = apiClient.inquiryOwnership(token, req);
            if(log.isDebugEnabled()){
                log.debug("Shahkr response is:{}",resp);
            }
            return mapToStatus(resp);

        } catch (RestClientResponseException e) {
            // HTTP error from Shahkar
            log.warn("Shahkar HTTP error. status={}, body={}", e.getStatusCode(), safeBody(e.getResponseBodyAsString()), e);
            return ShahkarStatus.FAIL;
        } catch (Exception e) {
            log.warn("Shahkar error (FAIL).", e);
            return ShahkarStatus.FAIL;
        }
    }

    private ShahkarStatus mapToStatus(ShahkarInquiryResponse resp) {
        // Placeholder mapping:
        // later: map real API fields/codes
        if (resp == null || !resp.done() ){
            log.error(" SHAHKAR-SERVICE Failed. response is:{}",resp);
            return ShahkarStatus.FAIL;
        }

        if ("OK.".equals(resp.result().status())) {
            return ShahkarStatus.OWNED;
        }
        else  {
            return ShahkarStatus.NOT_OWNED;
        }

    }

    private static String mask(String s) {
        if (s == null || s.length() < 4) return "***";
        return "***" + s.substring(s.length() - 4);
    }

    private static String maskMobile(String s) {
        if (s == null || s.length() < 4) return "***";
        return "****" + s.substring(s.length() - 4);
    }

    private static String safeBody(String body) {
        if (body == null) return "";
        return body.length() > 500 ? body.substring(0, 500) + "..." : body;
    }
}