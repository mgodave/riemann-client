package org.robotninjas.riemann.sample;

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.robobninjas.riemann.RiemannConnection;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import static com.google.common.base.Throwables.propagate;

public class ClientWorker implements Runnable {

  private final Meter send;
  private final Meter ack;
  private final Timer rtt;
  private final RiemannConnectionPool pool;

  @Inject ClientWorker(@SendMeter Meter send, @AckMeter Meter ack, @LatencyTimer Timer rtt, RiemannConnectionPool pool) {
    this.send = send;
    this.ack = ack;
    this.rtt = rtt;
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

        send.mark();
        //final TimerContext ctx = rtt.time();

        pool.returnObject(connection);

        Futures.addCallback(isOk, new FutureCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            ack.mark();
            //ctx.stop();
          }

          @Override
          public void onFailure(Throwable t) {
          }
        });

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
