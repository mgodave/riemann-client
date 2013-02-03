package org.robotninjas.riemann.sample;

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.yammer.metrics.core.Meter;
import org.robobninjas.riemann.RiemannConnection;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import static com.google.common.base.Throwables.propagate;

class ClientWorker implements Runnable {

  private final Meter eventMeter;
  private final RiemannConnectionPool pool;

  ClientWorker(Meter eventMeter, RiemannConnectionPool pool) {
    this.eventMeter = eventMeter;
    this.pool = pool;
  }

  @Override
  public void run() {
    try {
      while (!Thread.currentThread().isInterrupted()) {

        final RiemannConnection connection = pool.borrowObject();
        final ListenableFuture<Boolean> isOk = connection.sendEvent(
          Proto.Event
            .newBuilder()
            .setMetricF(1000000)
            .setService("thing")
            .build());
        pool.returnObject(connection);

        Futures.addCallback(isOk, new FutureCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            eventMeter.mark();
          }

          @Override
          public void onFailure(Throwable t) {

          }
        });
//
//        isOk.get();
//        eventMeter.mark();

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
}
