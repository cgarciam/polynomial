package home.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Aspect for measuring the execution time of public methods in the application.
 * This aspect intercepts all public methods within the `home` package and logs
 * their execution time if debug logging is enabled.
 */
@Aspect
@Component
@Slf4j
public class ExecutionTimeAspect { // NOPMD AtLeastOneConstructor

    /**
     * Measures the execution time of public methods in the `home` package.
     *
     * @param joinPoint the join point representing the intercepted method
     * @return the result of the intercepted method execution
     * @throws Throwable if the intercepted method throws any exception
     */
    @Around("execution(public * home..*(..))")
    public Object measureExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
        final long startTime = System.currentTimeMillis();
        final Object result = joinPoint.proceed();
        final long endTime = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Method {} executed in {} ms", joinPoint.getSignature(), endTime - startTime);
        }
        return result;
    }

}