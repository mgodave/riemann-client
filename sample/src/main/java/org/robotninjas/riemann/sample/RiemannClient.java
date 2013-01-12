/*

 Copyright 2012 David Rusek

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

*/

package org.robotninjas.riemann.sample;

import com.aphyr.riemann.Proto;
import com.google.common.base.Optional;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.robobninjas.riemann.RiemannConnection;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.util.concurrent.Future;

public class RiemannClient {

  public static void main(String[] args) {
    final Injector injector = Guice.createInjector(new ClientModule("localhost", 5555));
    final RiemannConnectionPool pool = injector.getInstance(RiemannConnectionPool.class);
    Optional<RiemannConnection> conn = Optional.absent();
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
        }
        try {
          pool.close();
        } catch (Exception e) {
        }
      }
    }
  }

}
