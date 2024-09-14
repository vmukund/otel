package com.example.aop.advices;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.InjectSpanToContext;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

//import com.example.InjectSpan;

@Aspect
public class InjectSpanToContextAdvice {

    private static Logger LOGGER = LoggerFactory.getLogger(InjectChildSpanToContextAdvice.class);

    @Around("@annotation(com.example.InjectSpanToContext) && execution(* *(..))")
    public Object injectSpan(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();

        Object object;
        System.out.println("In InjectSpanToContextAdvice!");
        LOGGER.info("In InjectSpanToContextAdvice!");
        try {

            if (openTelemetry != null) {

                MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
                Method method = signature.getMethod();
                InjectSpanToContext injectTraceSpan = method.getAnnotation(InjectSpanToContext.class);
                String spanName = injectTraceSpan.spanName();

                String className = proceedingJoinPoint.getSignature().getDeclaringTypeName();
                Tracer tracer = openTelemetry.getTracer(className);

                String methodName = proceedingJoinPoint.getSignature().getName();

                Span span = tracer.spanBuilder(spanName == null ? methodName : spanName).setParent(Context.current())
                        .startSpan();
                try (Scope scope = span.makeCurrent()) {
                    object = proceedingJoinPoint.proceed(); // Invoking InjectSpanToContext annotation added method
                } finally {
                    span.end();
                }

                return object;

            } else {
                object = proceedingJoinPoint.proceed();
                return object;
            }

        } catch (Exception e) {
            object = proceedingJoinPoint.proceed();
            return object;
        }

    }

}
