package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static com.aphyr.riemann.Proto.Event;
import static com.aphyr.riemann.Proto.Msg;

@NotThreadSafe
public class Connection implements Closeable {

  private final Channel channel;

  public Connection(Channel channel) {
    this.channel = channel;
  }

  public ListenableFuture<Boolean> sendEvent(Event e) throws InterruptedException {
    final Msg msg = Msg.newBuilder()
      .addEvents(e)
      .build();
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2) throws InterruptedException {
    final Msg msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .build();
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2, Event e3) throws InterruptedException {
    final Msg msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3)
      .build();
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2, Event e3, Event e4) throws InterruptedException {
    final Msg msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3)
      .addEvents(3, e4)
      .build();
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2, Event e3, Event e4, Event e5) throws InterruptedException {
    final Msg msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3)
      .addEvents(3, e4)
      .addEvents(3, e5)
      .build();
    return sendMsg(channel, new ReturnableEvent(msg));
  }

  public ListenableFuture<Boolean> sendEvents(Event... events) throws InterruptedException {
    final Msg.Builder msgBuilder = Msg.newBuilder();
    for (Event event : events) {
      msgBuilder.addEvents(event);
    }
    final Msg msg = msgBuilder.build();
    return sendMsg(channel, new ReturnableEvent(msg));
  }

  public ListenableFuture<List<Proto.Event>> query(String query) throws InterruptedException {
    final Msg msg = Msg.newBuilder()
      .setQuery(Proto.Query.newBuilder()
        .setString(query))
      .build();
    return sendMsg(channel, new ReturnableQuery(msg));
  }

  private static <T> ListenableFuture<T> sendMsg(Channel channel, ReturnableMessage<T> returnable) throws InterruptedException {
    final ChannelFuture writeFuture = channel.write(returnable);
    final SettableFuture returnableFuture = returnable.getFuture();
    writeFuture.sync();
    return returnableFuture;
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }

  protected Channel getChannel() {
    return channel;
  }

  /**
   * Simple class which handles cleaning-up the returned futures if the channel
   * operation was either cancelled or failed to send for some reason.
   */
  private static class ChannelFutureBridge implements ChannelFutureListener {

    private final SettableFuture<?> returnableFuture;

    public ChannelFutureBridge(SettableFuture<?> returnableFuture) {
      this.returnableFuture = returnableFuture;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) throws Exception {
      if (channelFuture.isDone() && channelFuture.getCause() != null) {
        // some failure along the pipeline
        returnableFuture.setException(channelFuture.getCause());
      } else if (channelFuture.isDone() && channelFuture.isCancelled()) {
        // this io operation was canceled
        returnableFuture.cancel(true);
      }
    }
  }

}
