package org.robotninjas.riemann.sample;

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import org.robobninjas.riemann.RiemannConnection;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Throwables.propagate;

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
      executor.execute(new Runnable() {
        @Override public void run() {
          try {
            for (; ; ) {

              final RiemannConnection connection = pool.borrowObject();
              final ListenableFuture<Boolean> isOk = connection.sendEvent(
                Proto.Event
                  .newBuilder()
                  .setMetricF(1000000)
                  .setService("thing")
                  .build());
              pool.returnObject(connection);

              isOk.get();
              eventMeter.mark();

            }

          } catch (Throwable t) {
            propagate(t);
          } finally {
            try {
              pool.close();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      });

    }
    notifyStarted();
  }

  @Override
  protected void doStop() {
    notifyStopped();
  }

}
