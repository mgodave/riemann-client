[![Build Status](https://travis-ci.org/mgodave/riemann-client.png)](https://travis-ci.org/mgodave/riemann-client)

About
-----

This is a simple Java client for [Riemann](https://github.com/aphyr/riemann). It is based on [Netty](http://netty.io) and uses
[Guava](http://code.google.com/p/guava-libraries/) futures for results. The code is also JSR305 compatible; I prefer to use Guice.

Get It
------

```xml

<repository>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <id>central</id>
    <name>bintray</name>
    <url>http://dl.bintray.com/content/mgodave/robotninjas</url>
</repository>

<dependency>
    <groupId>org.robotninjas.riemann</groupId>
    <artifactId>client</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>org.robotninjas.riemann</groupId>
    <artifactId>pool</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>org.robotninjas.riemann</groupId>
    <artifactId>guice</artifactId>
    <version>1.0.0</version>
</dependency>

<dependency>
    <groupId>org.robotninjas.riemann</groupId>
    <artifactId>load-test</artifactId>
    <version>1.0.0</version>
</dependency>

```

Use It
------

```java
package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.io.Closeables.closeQuietly;
import static makeClient;

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

      Futures.addCallback(isOk, new FutureCallback<Boolean>() {
        @Override
          public void onSuccess(Boolean result) {
            System.out.println("It's OK");
          }

          @Override
          public void onFailure(Throwable t) {
          }
      });

    } catch (Throwable t) {
      propagate(t);
    } finally {
      closeQuietly(connection);
      client.shutdown();
    }

  }

}
```

Note on JSR330
--------------

Both of the client implementations have constructors annotated with the @Inject annotation. These constructors are
also make use the Guice's AssistedInject extension and annotate the appropriate constructor arguments. The beautiful 
thing about annotations is that you do not need to include the dependency if you are not using them.



