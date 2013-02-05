package org.robotninjas.riemann.load;

import com.aphyr.riemann.Proto;
import com.google.common.base.Supplier;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.robobninjas.riemann.guice.RiemannClientModule;

import java.util.concurrent.TimeUnit;

public class LoadTest {

  //TODO use commons-config and commons-cli so I can script this...
  private static final int NUM_CLIENT_WORKERS = 5;
  private static final int BATCH_SIZE = 400;
  private static final int NUM_CONNECTIONS = 5;
  private static final int NUM_NETTY_WORKERS = 5;
  private static final int BUFFER_SIZE = 16384;

  public static void main(String[] args) {

    final GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
    poolConfig.maxActive = NUM_CONNECTIONS;

    final Injector injector = Guice.createInjector(
        new RiemannClientModule("localhost", 5555, NUM_NETTY_WORKERS, poolConfig, BUFFER_SIZE),
        new LoadTestModule(NUM_CLIENT_WORKERS, BATCH_SIZE, new DefaultEventSupplier()));

    final ConsoleReporter consoleReporter = injector.getInstance(ConsoleReporter.class);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        consoleReporter.shutdown();
      }
    });
    consoleReporter.start(1, TimeUnit.SECONDS);

    final LoadTestService loadTestService = injector.getInstance(LoadTestService.class);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        loadTestService.stopAndWait();
      }
    });
    loadTestService.startAndWait();

  }

  private static class DefaultEventSupplier implements Supplier<Proto.Event> {

    private final Proto.Event.Builder builder = Proto.Event.newBuilder();

    public DefaultEventSupplier() {
      builder.setMetricF(1000000).setService("thing");
    }

    @Override public Proto.Event get() {
      return builder.build();
    }
  }

}
