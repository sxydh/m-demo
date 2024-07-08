package cn.net.bhe.aopdemo.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AspectDemo {

    @Pointcut("execution(public void cn.net.bhe.aopdemo.ComponentDemo.run(..))")
    public void any() {
    }

    @Before("any()")
    public void before(JoinPoint joinPoint) {
        System.out.println("before");
    }

    @Around(value = "any()", argNames = "proceedingJoinPoint")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        try {
            System.out.println("around start");
            return proceedingJoinPoint.proceed();
        } finally {
            System.out.println("around end");
        }
    }

    @AfterReturning("any()")
    public void afterReturning(JoinPoint joinPoint) {
        System.out.println("afterReturning");
    }

    @AfterThrowing("any()")
    public void afterThrowing(JoinPoint joinPoint) {
        System.out.println("afterThrowing");
    }

    @After("any()")
    public void after(JoinPoint joinPoint) {
        System.out.println("after");
    }

}
