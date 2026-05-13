package ir.daneshrefah.scm.cache.client.utility.concurrencylimit.aspect;

import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.ConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.annotation.WithConcurrencyLimit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithConcurrencyLimitAspectTest {

    @Mock
    private ConcurrencyLimiterUtility concurrencyLimiterUtility;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature methodSignature;

    @Test
    void buildsSemaphoreKeyFromObjectFieldViaSpel() throws Throwable {
        WithConcurrencyLimitAspect aspect = new WithConcurrencyLimitAspect(concurrencyLimiterUtility);
        Method method = SampleService.class.getMethod("sendOtp", String.class, SemaphoreUser.class);
        WithConcurrencyLimit withConcurrencyLimit = method.getAnnotation(WithConcurrencyLimit.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new SampleService());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"ignored", new SemaphoreUser(91L)});
        when(joinPoint.proceed()).thenReturn("ok");
        when(concurrencyLimiterUtility.executeWithConcurrencyLimit(anyString(), anyInt(), any(), any())).thenAnswer(invocation -> {
            Callable<?> job = invocation.getArgument(3);
            return job.call();
        });

        Object result = aspect.applyConcurrencyLimit(joinPoint, withConcurrencyLimit);

        assertEquals("ok", result);
        ArgumentCaptor<String> limitNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> maxCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Duration> waitCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(concurrencyLimiterUtility).executeWithConcurrencyLimit(limitNameCaptor.capture(), maxCaptor.capture(), waitCaptor.capture(), any());
        assertEquals("otp-send::uid::91", limitNameCaptor.getValue());
        assertEquals(3, maxCaptor.getValue());
        assertEquals(Duration.ofMillis(900), waitCaptor.getValue());
    }

    @Test
    void rejectsConflictingPerUserAndGlobalFlags() throws Throwable {
        WithConcurrencyLimitAspect aspect = new WithConcurrencyLimitAspect(concurrencyLimiterUtility);
        Method method = SampleService.class.getMethod("invalid");
        WithConcurrencyLimit withConcurrencyLimit = method.getAnnotation(WithConcurrencyLimit.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn(method.getName());
        when(methodSignature.getDeclaringTypeName()).thenReturn(method.getDeclaringClass().getName());

        assertThrows(IllegalArgumentException.class, () -> aspect.applyConcurrencyLimit(joinPoint, withConcurrencyLimit));
        verifyNoInteractions(concurrencyLimiterUtility);
    }

    private static class SampleService {

        @WithConcurrencyLimit(name = "otp-send", key = "'uid::' + #p1.id", maxConcurrentExecutions = 3, waitMillis = 900)
        public String sendOtp(String ignored, SemaphoreUser user) {
            return "ok";
        }

        @WithConcurrencyLimit(name = "invalid", perUser = true, global = true)
        public void invalid() {
        }
    }

    private static class SemaphoreUser {

        private final Long id;

        private SemaphoreUser(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
