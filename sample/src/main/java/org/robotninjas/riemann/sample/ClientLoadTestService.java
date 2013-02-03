package org.robotninjas.riemann.sample;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class ClientLoadTestService extends AbstractService {

  private final RiemannConnectionPool pool;
  private final Executor executor;
  private final Meter eventMeter;
  private final int workers;

  @Inject
  public ClientLoadTestService(RiemannConnectionPool pool, Executor executor, MetricsRegistry registry, @WorkerCount int workers) {
    this.pool = pool;
    this.executor = executor;
    this.eventMeter = registry.newMeter(getClass(), "events", "events", TimeUnit.SECONDS);
    this.workers = workers;
  }

  @Override
  protected void doStart() {

    for (int i = 0; i < workers; i++) {
      executor.execute(new ClientWorker(eventMeter, pool));
    }
    notifyStarted();

  }

  @Override
  protected void doStop() {
    notifyStopped();
  }

}
