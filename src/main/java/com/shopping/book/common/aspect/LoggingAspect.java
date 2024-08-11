package com.shopping.book.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private ErrorLogRepository errorLogRepository;

    // 공통 포인트컷 정의
    @Pointcut("execution(* com.shopping.book..controller.*.*(..)) || " +
            "execution(* com.shopping.book..service.*.*(..)) || " +
            "execution(* com.shopping.book..repository.*.*(..))")
    public void applicationPackagePointcut() {
        // 포인트컷 메서드 본문은 비워둠
    }

    @Before("applicationPackagePointcut()")
    public void logBeforeMethods(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("Entering method: {} with arguments: {}", methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "applicationPackagePointcut()", returning = "result")
    public void logAfterMethods(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("Exiting method: {} with result: {}", methodName, result);
    }

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String arguments = Arrays.toString(joinPoint.getArgs());
        String stackTrace = Arrays.toString(exception.getStackTrace());

        log.error("Exception in method: {} in class: {} with arguments: {} and message: {}",
                methodName,
                className,
                arguments,
                exception.getMessage(),
                exception);

        // 예외 정보를 Elasticsearch에 저장
        ErrorLog errorLog = new ErrorLog(exception.getMessage(), stackTrace, className, methodName, arguments);
        errorLogRepository.save(errorLog);
    }
}
