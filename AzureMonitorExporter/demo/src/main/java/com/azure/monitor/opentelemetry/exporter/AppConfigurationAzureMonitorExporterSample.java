package com.azure.monitor.opentelemetry.exporter;

import java.lang.foreign.MemorySegment.Scope;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Sample to demonstrate using {@link AzureMonitorTraceExporter} to export
 * telemetry events when setting a configuration
 * in App Configuration through the {@link ConfigurationClient}.
 */
public class AppConfigurationAzureMonitorExporterSample {

    private static final Tracer TRACER = configureAzureMonitorExporter();
    private static final String CONNECTION_STRING = "InstrumentationKey=2e13fe39-99b0-4d0f-b4ff-6d918edab6b3;IngestionEndpoint=https://centralindia-0.in.applicationinsights.azure.com/;LiveEndpoint=https://centralindia.livediagnostics.monitor.azure.com/";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */

    public static void main(String[] args) {
        doClientWork();
    }

    /**
     * Configure the OpenTelemetry {@link SpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureAzureMonitorExporter() {
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        new AzureMonitorExporterBuilder()
                .connectionString("{connection-string}").install(sdkBuilder);

        /*
         * AzureMonitorExporterBuilder azureMonitorExporterBuilder = new
         * AzureMonitorExporterBuilder()
         */
        /* .connectionString("{connection-string}"); */
        /* azureMonitorExporterBuilder.buildExporter(); */

        OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();

        return openTelemetry.getTracer("Sample");
    }

    /**
     * Creates the {@link ConfigurationClient} and sets a configuration in Azure App
     * Configuration with distributed
     * tracing enabled and using the Azure Monitor exporter to export telemetry
     * events to Azure Monitor.
     */
    private static void doClientWork() {
        ConfigurationClient client = new ConfigurationClientBuilder()
                .connectionString(CONNECTION_STRING)
                .buildClient();

        Span span = ((io.opentelemetry.api.trace.Tracer) TRACER).spanBuilder("user-parent-span").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            // Thread bound (sync) calls will automatically pick up the parent span and you
            // don't need to pass it explicitly.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            span.end();
            scope.close();
        }
    }
}
