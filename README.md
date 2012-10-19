About
-----

This is a simple Java client for [Riemann](https://github.com/aphyr/riemann). It is based on [Netty](http://netty.io) and uses
[Guava](http://code.google.com/p/guava-libraries/) futures for results. The code is also JSR305 compatible; I prefer to use Guice.

Note: the UDP client is fundamentally broken in terms of being able to depend on the future result. Since UDP is an unreliable
protocol and the Riemann protocol doesn't allow us to tag requests or results with a sequence we have no way of telling which
response matches which which request. One out of order or missing return will completely screw up the client state.

Sample Usage
------------

```java
package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.io.Closeables.closeQuietly;
import static org.robobninjas.riemann.Clients.makeTcpClient;

public class SampleClient {

  public static void main(String[] args) {

    final Client client = makeTcpClient("localhost");
    Connection connection = null;

    try {

      connection = client.makeConnection();
      final Future<Boolean> isOk = connection.sendEvent(
          Proto.Event
              .newBuilder()
              .setMetricF(1000000)
              .setService("thing")
              .build());

      isOk.get(1, TimeUnit.SECONDS);

    } catch (Throwable t) {
      propagate(t);
    } finally {
      closeQuietly(connection);
      client.shutdown();
    }

  }

}
```