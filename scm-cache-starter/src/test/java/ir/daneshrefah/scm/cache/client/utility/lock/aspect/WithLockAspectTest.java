package ir.daneshrefah.scm.cache.client.utility.lock.aspect;

import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.annotation.WithLock;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithLockAspectTest {

    @Mock
    private LockUtility lockUtility;
    @Mock
    private ProceedingJoinPoint joinPoint;
    @Mock
    private MethodSignature methodSignature;

    @Test
    void buildsLockKeyFromObjectFieldViaSpel() throws Throwable {
        WithLockAspect aspect = new WithLockAspect(lockUtility);
        Method method = SampleService.class.getMethod("updateUser", String.class, LockUser.class);
        WithLock withLock = method.getAnnotation(WithLock.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getTarget()).thenReturn(new SampleService());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"ignored", new LockUser(73L)});
        when(joinPoint.proceed()).thenReturn("ok");
        when(lockUtility.executeWithLock(anyString(), any(), any())).thenAnswer(invocation -> {
            Callable<?> job = invocation.getArgument(2);
            return job.call();
        });

        Object result = aspect.applyLock(joinPoint, withLock);

        assertEquals("ok", result);
        ArgumentCaptor<String> lockNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> waitCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(lockUtility).executeWithLock(lockNameCaptor.capture(), waitCaptor.capture(), any());
        assertEquals("user-update::uid::73", lockNameCaptor.getValue());
        assertEquals(Duration.ofMillis(1200), waitCaptor.getValue());
    }

    @Test
    void rejectsConflictingPerUserAndGlobalFlags() throws Throwable {
        WithLockAspect aspect = new WithLockAspect(lockUtility);
        Method method = SampleService.class.getMethod("invalid");
        WithLock withLock = method.getAnnotation(WithLock.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn(method.getName());
        when(methodSignature.getDeclaringTypeName()).thenReturn(method.getDeclaringClass().getName());

        assertThrows(IllegalArgumentException.class, () -> aspect.applyLock(joinPoint, withLock));
        verifyNoInteractions(lockUtility);
    }

    private static class SampleService {

        @WithLock(name = "user-update", key = "'uid::' + #p1.id", waitMillis = 1200)
        public String updateUser(String ignored, LockUser user) {
            return "ok";
        }

        @WithLock(name = "invalid", perUser = true, global = true)
        public void invalid() {
        }
    }

    private static class LockUser {

        private final Long id;

        private LockUser(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }
    }
}
