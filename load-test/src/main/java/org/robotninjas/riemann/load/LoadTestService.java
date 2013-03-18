package org.robotninjas.riemann.load;

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import org.robotninjas.riemann.load.annotations.WorkExecutor;
import org.robotninjas.riemann.load.annotations.WorkerCount;

import java.util.concurrent.Executor;

public class LoadTestService extends AbstractService {

  private final Supplier<ClientWorker> workerFactory;
  private final Executor executor;
  private final int workers;

  @Inject
  public LoadTestService(Supplier<ClientWorker> workerFactory, @WorkExecutor Executor executor, @WorkerCount int workers) {
    this.workerFactory = workerFactory;
    this.executor = executor;
    this.workers = workers;
  }

  @Override
  protected void doStart() {

    for (int i = 0; i < workers; i++) {
      executor.execute(workerFactory.get());
    }
    notifyStarted();

  }

  @Override
  protected void doStop() {
    notifyStopped();
  }

}
