package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;
import com.google.common.io.Closeables;

public class SampleClient {

  public static void main(String[] args) {

    Client client = null;
    Connection connection = null;
    try {
      client = Clients.makeTcpClient("localhost");
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
