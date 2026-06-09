package ir.daneshrefah.scm.uaa.service.shahkar.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "scm.shahkar")
public class ShahkarProperties {

    /** Base URL of Shahkar REST API, e.g. https://shahkar.example.com */
    @Value("${service.shahkar.baseUrl}")
    private String baseUrl;

    /** Token endpoint path (relative or absolute) */

    @Value("${service.shahkar.tokenPath}")
    private String tokenPath ;

    /** Inquiry endpoint path (relative or absolute) */
    @Value("${service.shahkar.inquiryPath}")
    private String inquiryPath = "/api/v1/inquiry";

    /** client credentials / api key etc. (placeholder) */
    @Value("${service.shahkar.clientId}")
    private String clientId;
    @Value("${service.shahkar.clientSecret}")
    private String clientSecret;

    @Value("${service.shahkar.grant_Type}")
    private String grantType;

    /** How long we consider token valid if response doesn't include expires_in (fallback) */
    private Duration tokenTtlFallback = Duration.ofMinutes(15);

    /** Refresh token a bit earlier than actual expiry */
    private Duration tokenRefreshSkew = Duration.ofMinutes(2);

    /** HTTP timeouts */
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(5);

    /** Hazelcast map name */
    private String tokenCacheMap = "scm-uaa:shahkar:token-cache";
}