package org.robotninjas.riemann.sample;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.ConsoleReporter;
import com.yammer.metrics.reporting.CsvReporter;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoadTestModule extends PrivateModule {

  public static final MetricName SEND_RATE_METRIC_NAME = new MetricName(ClientWorker.class, "sends");
  public static final MetricName ACK_RATE_METRIC_NAME = new MetricName(ClientWorker.class, "acks");
  public static final MetricName LATENCY_TIMER_NAME = new MetricName(ClientWorker.class, "rtt");
  private final int workers;
  private final int batchSize;

  public LoadTestModule(int workers, int batchSize) {
    this.workers = workers;
    this.batchSize = batchSize;
  }

  @Override
  protected void configure() {
    bind(Integer.class).annotatedWith(WorkerCount.class).toInstance(workers);
    bind(Integer.class).annotatedWith(BatchSize.class).toInstance(batchSize);
    bind(ClientWorker.class);
    install(new FactoryModuleBuilder().build(ClientWorkerFactory.class));
    bind(LoadTestService.class);
    expose(LoadTestService.class);
  }

  @Provides
  @Exposed
  @Singleton
  public MetricsRegistry getMetricsRegistry() {
    return new MetricsRegistry();
  }

  @Provides
  @Exposed
  @Singleton
  @WorkExecutor
  public Executor getExecutor() {
    final ThreadFactoryBuilder builder = new ThreadFactoryBuilder();
    builder.setDaemon(false);
    builder.setNameFormat("Test Worker %d");
    final ExecutorService executor = Executors.newCachedThreadPool(builder.build());
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        executor.shutdown();
      }
    });
    return executor;
  }

  @Provides
  @LatencyTimer
  public Timer getLatencyTimer(MetricsRegistry registry) {
    return registry.newTimer(LATENCY_TIMER_NAME, TimeUnit.SECONDS, TimeUnit.SECONDS);
  }

  @Provides
  @AckMeter
  public Meter getAckMeter(MetricsRegistry registry) {
    return registry.newMeter(ACK_RATE_METRIC_NAME, "ack", TimeUnit.SECONDS);
  }

  @Provides
  @SendMeter
  public Meter getSendMeter(MetricsRegistry registry) {
    return registry.newMeter(SEND_RATE_METRIC_NAME, "send", TimeUnit.SECONDS);
  }

  @Provides
  @Exposed
  @Singleton
  public ConsoleReporter getConsoleReporter(MetricsRegistry registry) {
    return new ConsoleReporter(registry, System.out, MetricPredicate.ALL);
  }

  @Provides
  @Exposed
  @Singleton
  public CsvReporter getCsvReporter(MetricsRegistry registry) {
    return new CsvReporter(registry, new File(""));
  }

}
