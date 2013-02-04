package org.robotninjas.riemann.sample;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class LoadTestService extends AbstractService {

  private final ClientWorkerFactory workerFactory;
  private final Executor executor;
  private final int workers;

  @Inject
  public LoadTestService(ClientWorkerFactory workerFactory, @WorkExecutor Executor executor, @WorkerCount int workers) {
    this.workerFactory = workerFactory;
    this.executor = executor;
    this.workers = workers;
  }

  @Override
  protected void doStart() {

    for (int i = 0; i < workers; i++) {
      executor.execute(workerFactory.makeWorker());
    }
    notifyStarted();

  }

  @Override
  protected void doStop() {
    notifyStopped();
  }

}
