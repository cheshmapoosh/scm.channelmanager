package ir.daneshrefah.scm.uaa.rate.limit.aspect;

import ir.daneshrefah.scm.common.constant.ServiceBucket;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MethodRateLimitInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String methodPath;
    private ServiceBucket serviceBucket;
    private LimitType limitType;
}
