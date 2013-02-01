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
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.reporting.ConsoleReporter;
import com.yammer.metrics.reporting.CsvReporter;
import org.robobninjas.riemann.Clients;
import org.robobninjas.riemann.RiemannClient;
import org.robobninjas.riemann.RiemannConnection;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Throwables.propagate;

public class SampleClient {

  public static void main(String[] args) {

    final RiemannClient client = Clients.makeClient("localhost");
    final Meter eventMeter = Metrics.newMeter(SampleClient.class, "events", "events", TimeUnit.SECONDS);

    ConsoleReporter.enable(1, TimeUnit.SECONDS);
    //CsvReporter.enable(new File("/Users/drusek/reports"), 1, TimeUnit.SECONDS);

    final RiemannConnectionPool pool = new RiemannConnectionPool(client);
    final Executor executor = Executors.newCachedThreadPool();
    for (int i = 0; i < 8; i++) {
      executor.execute(new Runnable() {
        @Override public void run() {
          try {
            for (; ; ) {

              final RiemannConnection connection = pool.borrowObject();
              final Future<Boolean> isOk = connection.sendEvent(
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
            client.shutdown();
          }
        }
      });

    }
  }

}
