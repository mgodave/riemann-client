About
-----

This is a simple Java client for [Riemann](https://github.com/aphyr/riemann). It is based on [Netty](http://netty.io) and uses
[Guava](http://code.google.com/p/guava-libraries/) futures for results. The code is also JSR305 compatible; I prefer to use Guice.

Note: the UDP client is fundamentally broken in terms of being able to depend on the future result. I'm working on this.

Sample Usage
------------

    ```java
		package org.robobninjas.riemann;

		import com.aphyr.riemann.Proto;
		import com.google.common.io.Closeables;

		import static org.robobninjas.riemann.Clients.makeTcpClient;

		public class SampleClient {

		  public static void main(String[] args) {

		    final Client client = makeTcpClient("localhost");
		    Connection connection = null;

		    try {
		      connection = client.makeConnection();
		      connection.sendEvent(
		        Proto.Event
		          .newBuilder()
		          .setMetricF(1000000)
		          .setService("thing")
		          .build());

		    } catch (InterruptedException e) {
		      e.printStackTrace();
		    } finally {
		      Closeables.closeQuietly(connection);
		      client.shutdown();
		    }

		  }

		}
		```