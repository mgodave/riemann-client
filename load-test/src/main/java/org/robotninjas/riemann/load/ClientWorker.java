package org.robotninjas.riemann.load;

import com.aphyr.riemann.Proto;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import org.robotninjas.riemann.client.RiemannTcpConnection;
import org.robotninjas.riemann.load.annotations.*;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.util.concurrent.Executor;

import static com.google.common.base.Throwables.propagate;

public class ClientWorker implements Runnable {

  private final Meter send;
  private final Meter ack;
  private final Timer rtt;
  private final Meter eventAck;
  private final RiemannConnectionPool pool;
  private final int batchSize;
  private final Supplier<Proto.Event> eventSupplier;
  private final Executor ackExecutor;

  @Inject
  ClientWorker(@SendMeter Meter send, @AckMeter Meter ack, @LatencyTimer Timer rtt, @EventAckMeter Meter eventAck,
               @BatchSize int batchSize, RiemannConnectionPool pool, @EventSupplier Supplier<Proto.Event> eventSupplier,
               @AckExecutor Executor ackExecutor) {

    this.send = send;
    this.ack = ack;
    this.rtt = rtt;
    this.eventAck = eventAck;
    this.pool = pool;
    this.batchSize = batchSize;
    this.eventSupplier = eventSupplier;
    this.ackExecutor = ackExecutor;

  }

  @Override
  public void run() {

    ImmutableList.Builder<Proto.Event> eventsBuilder = ImmutableList.builder();
    for (int i = 0; i < batchSize; i++) {
      eventsBuilder.add(eventSupplier.get());
    }

    final ImmutableList<Proto.Event> events = eventsBuilder.build();

    try {

      while (!Thread.currentThread().isInterrupted()) {

        final RiemannTcpConnection connection = pool.borrowObject();

        final ListenableFuture<Boolean> isOk = connection.sendWithAck(events);
        send.mark();
        final TimerContext ctx = rtt.time();

        pool.returnObject(connection);

        Futures.addCallback(isOk, new FutureCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            eventAck.mark(batchSize);
            ack.mark();
            ctx.stop();
          }

          @Override
          public void onFailure(Throwable t) {
          }
        }, ackExecutor);

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
