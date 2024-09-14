package com.example.emittrace;

import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;

// import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;

public class ConfigureOTel {

        public static OpenTelemetry initializeOpenTelemetryForTML() {

                // Add hook to close SDK, which flushes logs

                // final String otelColEndpoint =
                // PropertiesCache.getInstance().getProperty("OTEL_COLLECTOR_ENDPOINT");

                String otelColEndpoint = (System.getenv("OTEL_COLLECTOR_ENDPOINT"));

                if (otelColEndpoint == null) {
                        otelColEndpoint = PropertiesCache.getInstance().getProperty("OTEL_COLLECTOR_ENDPOINT");
                }
                System.out.println("Export Endpoint " + otelColEndpoint);

                Resource serviceResource = Resource
                                .create(Attributes.of(AttributeKey.stringKey("service.name"), "TML-Service"));

                OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                                .setTimeout(4, TimeUnit.SECONDS)
                                .setEndpoint(otelColEndpoint)
                                .build();

                BatchSpanProcessor spanProcessor = BatchSpanProcessor.builder(spanExporter)
                                .setScheduleDelay(100, TimeUnit.MILLISECONDS)
                                .build();

                OtlpGrpcLogRecordExporter logExporter = OtlpGrpcLogRecordExporter.builder()
                                .setTimeout(2, TimeUnit.SECONDS)
                                .setEndpoint(otelColEndpoint)
                                .build();

                BatchLogRecordProcessor logBatchLogProcessor = BatchLogRecordProcessor.builder(logExporter).build();

                SdkLoggerProvider sdkloggerProvider = SdkLoggerProvider.builder()
                                .setResource(serviceResource)
                                .addLogRecordProcessor(logBatchLogProcessor)
                                .build();

                SdkTracerProvider sdktracerProvider = SdkTracerProvider.builder()
                                .setResource(serviceResource)
                                .addSpanProcessor(spanProcessor)
                                .setSampler(Sampler.alwaysOn())
                                .build();

                OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.builder()
                                .setEndpoint(otelColEndpoint)
                                .build();
                PeriodicMetricReader metricReader = PeriodicMetricReader.builder(metricExporter).build();

                /*
                 * // Use this when you want to directly export to Prometheus
                 * int prometheusPort = 9464;
                 * SdkMeterProvider sdkMeterProviderPrometheus = SdkMeterProvider.builder()
                 * .registerMetricReader(PrometheusHttpServer.builder().setPort(prometheusPort).
                 * build())
                 * .setResource(serviceResource)
                 * .build();
                 */

                SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                                .registerMetricReader(metricReader)
                                .setResource(serviceResource)
                                .build();

                OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                                .setLoggerProvider(sdkloggerProvider)
                                .setTracerProvider(sdktracerProvider)
                                .setMeterProvider(sdkMeterProvider)
                                .buildAndRegisterGlobal();

                Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));
                return sdk;
        }

}
