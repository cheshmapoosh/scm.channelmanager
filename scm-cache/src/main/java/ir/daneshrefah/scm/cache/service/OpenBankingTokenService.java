package ir.daneshrefah.scm.cache.service;

import ir.daneshrefah.scm.common.token.OpenBankingToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenBankingTokenService {
    private static final Log LOGGER = LogFactory.getLog(OpenBankingTokenService.class);
    private final WebClient.Builder webClientBuilder;
    private final HazelCastService hazelCastService;
    private static final String OPEN_BANKING_TOKEN_CACHE_MAP = "open-banking-token-cache";
    private static final String OPEN_BANKING_TOKEN_KEY = "open-banking-token";


    @Value("${open-banking.uri}")
    private String uri;

  /*  @Scheduled(fixedRate = 3600000)*/
    public void getToken() {
        removeTokenFromCache();

        WebClient client = webClientBuilder.build();

        OpenBankingToken openBankingToken = client
                .post()
                .uri(uri)
                .headers(httpHeaders -> {
                    httpHeaders.set("Content-Type", "application/x-www-form-urlencoded");
                    httpHeaders.set("Cookie", "cookiesession1=678B2888F77EA7F12CF5F6CEC705CEBB; cookiesession1=678B2883270C75A3A02D4B989BCAF1FF");
                })
                .body(BodyInserters
                        .fromFormData("grant_type", "client_credentials")
                        .with("client_id", "cm_develop")
                        .with("client_secret", "9jl48zohr0cqa6wnc54h"))
                .retrieve()
                .bodyToMono(OpenBankingToken.class)
                .block();

        if (openBankingToken == null) {
            LOGGER.error("openBankingToken is null");
            return;
        }

        LOGGER.info("OpenBanking token retrieval done!");
        putTokenInCache(openBankingToken);
    }

    private void removeTokenFromCache() {
        Object token = hazelCastService.getFromCache(OPEN_BANKING_TOKEN_CACHE_MAP, OPEN_BANKING_TOKEN_KEY);
        if (token != null) {
            hazelCastService.removeFromCache(OPEN_BANKING_TOKEN_CACHE_MAP, OPEN_BANKING_TOKEN_KEY);
        }
    }

    private void putTokenInCache(OpenBankingToken openBankingToken) {
        hazelCastService.putInCache(OPEN_BANKING_TOKEN_CACHE_MAP, OPEN_BANKING_TOKEN_KEY, openBankingToken);
    }

}
