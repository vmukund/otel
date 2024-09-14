package com.example.emittrace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.InjectSpan;
import com.example.InjectSpanToContext;

import io.opentelemetry.api.OpenTelemetry;

@RestController
public class HelloController {

    public static OpenTelemetry openTelemetry = OpenTelemetryManager.initOpenTelemetry();

    private static Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @GetMapping("/hello")
    public String index() throws InterruptedException {
        LOGGER.info("App is started!-In Controller -Hello");
        login();
        LOGGER.info("Exported Traces!!!");
        return "Greetings from Spring Boot!";
    }

    @InjectSpan(spanName = "Login")
    private void login() throws InterruptedException {
        Thread.sleep(1000);
        LOGGER.info("In Login!!!");
        dashboard();

    }

    @InjectSpanToContext(spanName = "Dashboard")
    private void dashboard() throws InterruptedException {
        Thread.sleep(1000);
        LOGGER.info("In Dashboard!!!");

    }
}
