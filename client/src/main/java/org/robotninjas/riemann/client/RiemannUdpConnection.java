package org.robotninjas.riemann.client;

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.ListenableFuture;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class RiemannUdpConnection implements RiemannConnection {

  private final Channel channel;
  private final ConnectionlessBootstrap bootstrap;

  public RiemannUdpConnection(Channel channel, ConnectionlessBootstrap bootstrap) {
    this.channel = channel;
    this.bootstrap = bootstrap;
  }

  public void send(Proto.Event e) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addEvents(e);
    channel.write(msg);
  }

  public void send(Iterable<Proto.Event> events) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addAllEvents(events);
    channel.write(msg);
  }

  @Override
  public boolean isOpen() {
    return channel.isOpen();
  }

  public ListenableFuture<List<Proto.Event>> query(String query) throws ExecutionException, InterruptedException {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .setQuery(Proto.Query.newBuilder()
        .setString(query));
    final ReturnableQuery queryMessage = new ReturnableQuery(msg);
    channel.write(queryMessage);
    return queryMessage;
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }

  protected Channel getChannel() {
    return channel;
  }

}
