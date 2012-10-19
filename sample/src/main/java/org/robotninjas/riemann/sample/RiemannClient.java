package org.robotninjas.riemann.sample;

import com.aphyr.riemann.Proto;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.robobninjas.riemann.Connection;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.util.concurrent.Future;

public class RiemannClient {

  public static void main(String[] args) {
    final Injector injector = Guice.createInjector(new ClientModule("localhost", 5555));
    final RiemannConnectionPool pool = injector.getInstance(RiemannConnectionPool.class);
    Optional<Connection> conn = Optional.absent();
    try {
      conn = Optional.of(pool.borrowObject());
      final Future<Boolean> isOk = conn.get().sendEvent(Proto.Event
        .newBuilder()
        .setService("MyService")
        .setMetricF(1000000000)
        .build());
      System.out.println(isOk.get());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (conn.isPresent()) {
        try {
          pool.returnObject(conn.get());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

}
