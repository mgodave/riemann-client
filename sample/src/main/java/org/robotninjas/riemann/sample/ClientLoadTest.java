package org.robotninjas.riemann.sample;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.robobninjas.riemann.guice.RiemannClientModule;

import java.util.concurrent.TimeUnit;

public class ClientLoadTest {

  private static final int WORKER_COUNT = 8;
  private static final int ACTIVE_CONNECTIONS = 4;
  private static final int NETTY_WORKER_COUNT = 2;

  public static void main(String[] args) {

    final GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
    poolConfig.maxActive = ACTIVE_CONNECTIONS;

    final Injector injector = Guice.createInjector(
      new RiemannClientModule("localhost", 5555, NETTY_WORKER_COUNT, poolConfig),
      new LoadTestModule(WORKER_COUNT));

    final MetricsRegistry registry = injector.getInstance(MetricsRegistry.class);
    ConsoleReporter.enable(registry, 1, TimeUnit.SECONDS);

    final ClientLoadTestService loadTestService = injector.getInstance(ClientLoadTestService.class);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        loadTestService.stopAndWait();
      }
    });
    loadTestService.startAndWait();
  }

}
