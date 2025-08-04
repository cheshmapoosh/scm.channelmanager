package ir.daneshrefah.scm.uaa.rate.limit.aspect;

import ir.daneshrefah.scm.cache.client.distribution.spec.DistributedRateLimiterService;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.model.ScmResponse;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Aspect
@RequiredArgsConstructor
public class RateLimitMonitor {

    private final DistributedRateLimiterService rateLimitService;
    private final ResourceBundleService bundleService;
    Map<String, MethodRateLimitInfo> METHOD_RATE_LIMIT_INFO_CACHE = new ConcurrentHashMap<>();

    @Around("@annotation(ir.daneshrefah.scm.uaa.rate.limit.aspect.RateLimit)")
    public Object monitor(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodRateLimitInfo info = getCurrentMethodAnnotation(joinPoint);
        String bucketKey = generateBucketKey(info);
        return applyRateLimitAll(joinPoint, info, bucketKey);
    }


    private Object applyRateLimitAll(ProceedingJoinPoint joinPoint, MethodRateLimitInfo info, String bucketKey) throws Throwable {
        if (rateLimitService.tryConsume(info.getServiceBucket().getBucketName(), bucketKey)) {
            return joinPoint.proceed();
        } else {
            return createErrorResponse();
        }
    }

    private ResponseEntity<?> createErrorResponse() {
        String englishMsg = bundleService.get(AccessibleLocale.EN_US.getLocale(), "tooManyRequest").orElse("too many request");
        String persianMsg = bundleService.get(AccessibleLocale.FA_IR.getLocale(), "tooManyRequest").orElse(null);
        Error error = new Error("rateLimit", 9000, englishMsg,persianMsg,MessageStatus.SC_ACCESS_DENIED,null);
        ScmResponse response = ScmResponse
                .builder()
                .errors(List.of(error))
                .status(MessageStatus.SC_ACCESS_DENIED)
                .build();
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(response);
    }

    private String generateBucketKey(MethodRateLimitInfo info) {
        LimitType limitType = info.getLimitType();
        if (limitType.equals(LimitType.PER_SERVICE)) {
            return info.getMethodPath();
        } else {
            Authentication authentication = AuthenticationUtils.getAuthentication();
            User user = (User) authentication.getPrincipal();
            Integer id = user.getPerson().getId();
            return info.getMethodPath() + "::uid" + id;
        }
    }

    private MethodRateLimitInfo getCurrentMethodAnnotation(ProceedingJoinPoint joinPoint) {
        String methodName = joinPoint.getStaticPart().getSignature().getName();
        Class<?> type = joinPoint.getSourceLocation().getWithinType();
        String cacheKey = type.getName() + "::" + methodName;
        return METHOD_RATE_LIMIT_INFO_CACHE.computeIfAbsent(cacheKey, key -> {
            Method method = ReflectionUtils.findMethod(type, methodName);
            assert method != null;
            RateLimit annotation = method.getAnnotation(RateLimit.class);
            MethodRateLimitInfo info = new MethodRateLimitInfo();
            info.setLimitType(annotation.type());
            info.setServiceBucket(annotation.bucket());
            info.setMethodPath(cacheKey);
            return info;
        });
    }

}
