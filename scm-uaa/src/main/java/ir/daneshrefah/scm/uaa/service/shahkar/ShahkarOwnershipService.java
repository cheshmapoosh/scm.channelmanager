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
            log.warn("Shahkar ownership check failed. errorType={}", e.getClass().getSimpleName());
            return ShahkarStatus.FAIL;
        }
    }

    private ShahkarStatus doCheck(String nationalId, String mobileNo) {
        try {
            String token = tokenProvider.getValidAccessToken();

            ShahkarInquiryRequest req = new ShahkarInquiryRequest(nationalId, mobileNo,"0");
            ShahkarInquiryResponse resp = apiClient.inquiryOwnership(token, req);
            log.debug("Shahkar ownership response received");
            return mapToStatus(resp);

        } catch (RestClientResponseException e) {
            log.warn("Shahkar HTTP error. status={}", e.getStatusCode());
            return ShahkarStatus.FAIL;
        } catch (Exception e) {
            log.warn("Shahkar request failed. errorType={}", e.getClass().getSimpleName());
            return ShahkarStatus.FAIL;
        }
    }

    private ShahkarStatus mapToStatus(ShahkarInquiryResponse resp) {
        // Placeholder mapping:
        // later: map real API fields/codes
        if (resp == null || !resp.done() ){
            log.error("Shahkar returned an incomplete ownership response");
            return ShahkarStatus.FAIL;
        }

        if ("OK.".equals(resp.result().status())) {
            return ShahkarStatus.OWNED;
        }
        else  {
            return ShahkarStatus.NOT_OWNED;
        }

    }

}
