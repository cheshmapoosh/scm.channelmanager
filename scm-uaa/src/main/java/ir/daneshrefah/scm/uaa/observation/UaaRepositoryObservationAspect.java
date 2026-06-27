package ir.daneshrefah.scm.uaa.observation;

import ir.daneshrefah.scm.observation.ObservationScope;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
@RequiredArgsConstructor
public class UaaRepositoryObservationAspect {
    private final UaaObservation observation;

    @Around("""
            execution(public * ir.daneshrefah.scm.uaa.repository..*(..)) ||
            execution(public * ir.daneshrefah.scm.common.data.repository..*(..))
            """)
    public Object observeRepository(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> repositoryType = signature.getMethod().getDeclaringClass();
        if (!repositoryType.getSimpleName().endsWith("Repository")) {
            return joinPoint.proceed();
        }

        String datasource = datasource(repositoryType);
        UaaObservation.DbContext ctx = new UaaObservation.DbContext(
                datasource,
                repositoryType.getSimpleName().replace("Repository", "") + "." + signature.getMethod().getName()
        );
        ObservationScope scope = "activation".equals(datasource)
                ? observation.traceDbActivation(ctx)
                : observation.traceDbAuthentication(ctx);
        try {
            Object result = joinPoint.proceed();
            scope.success();
            return result;
        } catch (Throwable ex) {
            scope.failure(ex);
            throw ex;
        } finally {
            scope.close();
        }
    }

    private String datasource(Class<?> repositoryType) {
        Package currentPackage = repositoryType.getPackage();
        String packageName = currentPackage == null ? "" : currentPackage.getName();
        return packageName.contains(".repository.activation") ? "activation" : "authentication";
    }
}
