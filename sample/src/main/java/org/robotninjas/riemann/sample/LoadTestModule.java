package org.robotninjas.riemann.sample;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.yammer.metrics.core.MetricsRegistry;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadTestModule extends PrivateModule {

  private final int workers;

  public LoadTestModule(int workers) {
    this.workers = workers;
  }

  @Override
  protected void configure() {
    bind(Integer.class).annotatedWith(WorkerCount.class).toInstance(workers);
    bind(ClientLoadTestService.class);
    expose(ClientLoadTestService.class);
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

}
