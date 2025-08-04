package ir.daneshrefah.scm.uaa.rate.limit.aspect;

import ir.daneshrefah.scm.common.constant.ServiceBucket;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    ServiceBucket bucket();
    LimitType type();
}
