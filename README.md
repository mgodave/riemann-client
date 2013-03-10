[![Build Status](https://travis-ci.org/mgodave/riemann-client.png)](https://travis-ci.org/mgodave/riemann-client)

About
-----

This is a simple Java client for [Riemann](https://github.com/aphyr/riemann). It is based on [Netty](http://netty.io) and uses
[Guava](http://code.google.com/p/guava-libraries/) futures for results. The code is also JSR305 compatible; I prefer to use Guice.

The current "stable" version is 1.0.0

This client sets up a netty pipeline with the following stages:

* ReturnableHandler
* ProtobufEncoder
* FrameEncoder
* BlockingWriteHandler

When data is sent, objects of type ReturnableMessage are sent down the pipeline. A ReturnableMessage is an AbstracFuture
with a payload. The ReturnableHandler strips the future from the payload and sends the payload further down the pipeline.
The future is enqueued and later mated with and ack from Riemann corresponding to this message. The payload is a protobuf message of type Msg
so the ProtobufEncoder encodes this message into a ChannelBuffer and passes it on. The encoded protobuf message is then
framed with a two byte length field by the FrameEncoder. Lastly, the framed message is passed to the BlockingWriteHandler.
The BlockingWriteHandler is a buffer for outgoing messages. If the channel is writeable the message is written directly to
the channel sink (the socket) and sent, otherwise it is enqueued until the channel becomes writeable again. Once the channel
is able to accept data again the buffer is flushed and all subsequent writes are forwarded on to the sink until the channel
once again becomes saturated and buffering begins again. The buffer is a BlockingQueue with a set, configurable, capacity.
When the buffer is full writes to the channel will block until it becomes writeable again. This has the effect of applying
a liberal amount of back pressure to a client which is producing enough data to saturate the connection. The back pressure
extends all of the way back from Riemann: Riemann, if saturated, will not be able to accept new data off the connection. The
receive buffer on riemann's interface will become full and it will begin to drop packets. Un-acked packets will cause the
client's send buffer to fill. Netty's NIO write buffer will fill. The BlockingWriteHandler will fill it's buffer. The client
will block. And on and on...

Load Test Tool
--------------

There is a Riemann load test tool included in this repo. To build it:

```bash
cd $CLIENT_HOME
mvn clean install
cd load-test
mvn assembly-single
```

There will be a tarball under in the target directory, this is a standalone version of the tool. To run, extract the tarball
and run ./bin/load-test.sh -h

The options available are:

```
usage: ./load-test.sh [options] [host:port]
 -b,--batch-size <arg>       number of Events to send in each Msg
 -c,--connections <arg>      number of concurrent connections
 -n,--netty-workers <arg>    number of netty worker threads
 -r,--reports-dir <arg>      base directory for generated reports
 -s,--buffer-size <arg>      netty pipeline buffer size in bytes
 -w,--client-workers <arg>   number of client workers
```

Reports are deposited in the CWD by default unless -r is specified. The tool uses Coda's Metrics Lib and the CsvReporter.
The csv files are all self documenting.

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



