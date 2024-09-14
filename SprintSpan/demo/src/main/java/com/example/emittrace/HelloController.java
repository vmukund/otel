package com.example.emittrace;

import org.apache.logging.log4j.LogManager;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
// import io.opentelemetry.api.trace.SpanKind;

@RestController
public class HelloController {

    private final Tracer tracer = GlobalOpenTelemetry.getTracer("MyInstrumentation");

    public static OpenTelemetry openTelemetry = ConfigureOTel.initializeOpenTelemetryForTML();
    private static final org.apache.logging.log4j.Logger log4jLogger = LogManager.getLogger("log4j-logger");

    @GetMapping("/hello")
    public String index() throws InterruptedException {
        // Install OpenTelemetry in log4j appender
        io.opentelemetry.instrumentation.log4j.appender.v2_17.OpenTelemetryAppender.install(openTelemetry);
        // Install OpenTelemetry in logback appender
        io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender.install(openTelemetry);
        // Route JUL logs to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        log4jLogger.info("-----------------Mukund------------------------");
        // log4jLogger.error("-----------------Mukund------------------------", new
        // Exception("error!"));

        Span parentSpan = addSpan(null, "MainSpan");

        log4jLogger.info("SpanID : {}", parentSpan.getSpanContext().getSpanId());

        parentSpan.setAttribute("EndPoint-Hello", "GET");
        parentSpan.addEvent("Init");
        parentSpan.addEvent("Starting the Login.");

        try {
            login(parentSpan);
            log4jLogger.info("----------------Exiting Now------------------------");
            return "Greetings from Spring Boot!";
        } finally {
            parentSpan.end();
        }

    }

    private void login(Span parentSpan) throws InterruptedException {
        // Thread.sleep(1000);
        log4jLogger.info("-----------------In Login------------------------");
        Span childSpan = addSpan(parentSpan, "Login1");

        dashboard(childSpan);

        childSpan.end();
    }

    private void dashboard(Span childspan) throws InterruptedException {
        // Thread.sleep(1000);
        log4jLogger.info("-----------------In Dashboard------------------------");

        Span grandChildSpan = addSpan(childspan, "Dashboard");

        exportMetrics(grandChildSpan);

        // Thread.sleep(1000);

        grandChildSpan.end();
    }

    private void exportMetrics(Span grandChildSpan) throws InterruptedException {

        Attributes attributes = Attributes
                .of(AttributeKey.stringKey("Account Balance"), "0",
                        AttributeKey.stringKey("Ledger Balance"), "-200");

        Meter meter = openTelemetry.getMeter("MyMeter");
        LongCounter counter = meter.counterBuilder("MyCounter").build();
        LongCounter counter1 = meter.counterBuilder("CounterWithAttiibutes")
                .setDescription("This is my Custom counter.")
                .setUnit("$")
                .build();

        counter1.add(1111, attributes);

        LongHistogram histogram = meter.histogramBuilder("MyHistogram").ofLongs().setUnit("ms").build();
        for (int i = 0; i < 300; i++) {
            long startTime = System.currentTimeMillis();
            Context exampleContext = Context.current().with(grandChildSpan);
            try (Scope scope = exampleContext.makeCurrent()) {
                counter.add(10);
                grandChildSpan.setAttribute("good", true);
                grandChildSpan.setAttribute("Counter-Value", i);
                // Thread.sleep(200);
            } finally {
                histogram.record(System.currentTimeMillis() - startTime, Attributes.empty(), exampleContext);
            }
        }
    }

    private Span addSpan(Span pSpan, String spanName) {
        if (pSpan == null) {
            return (tracer.spanBuilder(spanName).setNoParent().startSpan());
        } else
            return (tracer.spanBuilder(spanName).setParent(Context.current().with(pSpan)).startSpan());
    }
}
