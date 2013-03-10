package org.robotninjas.riemann.client;

import com.aphyr.riemann.Proto;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;

import java.io.IOException;

import static java.util.Arrays.asList;

public class FireAndForgetRiemannConnection implements RiemannConnection {

  private final Channel channel;
  private final ConnectionlessBootstrap bootstrap;

  public FireAndForgetRiemannConnection(Channel channel, ConnectionlessBootstrap bootstrap) {
    this.channel = channel;
    this.bootstrap = bootstrap;
  }

  public void sendEvent(Proto.Event e) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addEvents(e);
    channel.write(msg);

  }

  public void sendEvents(Proto.Event e1, Proto.Event e2) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2);
    channel.write(msg);

  }

  public void sendEvents(Proto.Event e1, Proto.Event e2, Proto.Event e3) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3);
    channel.write(msg);

  }

  public void sendEvents(Proto.Event e1, Proto.Event e2, Proto.Event e3, Proto.Event e4) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3)
      .addEvents(3, e4);
    channel.write(msg);

  }

  public void sendEvents(Proto.Event e1, Proto.Event e2, Proto.Event e3, Proto.Event e4, Proto.Event e5) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3)
      .addEvents(3, e4)
      .addEvents(4, e5);
    channel.write(msg);
  }

  public void sendEvents(Proto.Event... events) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addAllEvents(asList(events));
    channel.write(msg);
  }

  public void sendEvents(Iterable<Proto.Event> events) {
    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
      .addAllEvents(events);
    channel.write(msg);
  }

//  public List<Proto.Event> query(String query) throws ExecutionException, InterruptedException {
//    final Proto.Msg.Builder msg = Proto.Msg.newBuilder()
//      .setQuery(Proto.Query.newBuilder()
//        .setString(query));
//    return sendMsg(channel, new ReturnableQuery(msg)).get();
//  }

  @Override
  public void close() throws IOException {
    channel.close();
  }

  protected Channel getChannel() {
    return channel;
  }

}
