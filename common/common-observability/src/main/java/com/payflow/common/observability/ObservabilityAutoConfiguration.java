package com.payflow.common.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for PayFlow observability infrastructure.
 *
 * Activates:
 * 1. JVM metrics (GC, memory, threads, class loader)
 * 2. System metrics (CPU, uptime)
 * 3. TraceIdFilter for MDC propagation
 * 4. Global MeterFilter to add common tags to every metric
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
public class ObservabilityAutoConfiguration {

    /**
     * Adds application-level common tags to EVERY Micrometer metric registered in this service.
     * Prometheus query: payflow_payment_initiated_total{application="payment-service"}
     *
     * @param applicationName injected from spring.application.name
     */
    @Bean
    public MeterFilter commonTagsMeterFilter(
            @org.springframework.beans.factory.annotation.Value("${spring.application.name:unknown}") String applicationName
    ) {
        return MeterFilter.commonTags(
                io.micrometer.core.instrument.Tags.of(
                        "application", applicationName,
                        "environment", System.getProperty("spring.profiles.active", "default")
                )
        );
    }

    @Bean
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter();
    }

    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }
}
