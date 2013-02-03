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

package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.ListenableFuture;
import org.jboss.netty.channel.Channel;

import javax.annotation.concurrent.ThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import static com.aphyr.riemann.Proto.Event;
import static com.aphyr.riemann.Proto.Msg;
import static java.util.Arrays.asList;

@ThreadSafe
public class RiemannConnection implements Closeable {

  private final Channel channel;

  public RiemannConnection(Channel channel) {
    this.channel = channel;
  }

  public ListenableFuture<Boolean> sendEvent(Event e) throws InterruptedException {
    final Msg.Builder msg = Msg.newBuilder()
      .addEvents(e);
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2) throws InterruptedException {
    final Msg.Builder msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2);
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2, Event e3) throws InterruptedException {
    final Msg.Builder msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3);
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2, Event e3, Event e4) throws InterruptedException {
    final Msg.Builder msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3)
      .addEvents(3, e4);
    return sendMsg(channel, new ReturnableEvent(msg));

  }

  public ListenableFuture<Boolean> sendEvents(Event e1, Event e2, Event e3, Event e4, Event e5) throws InterruptedException {
    final Msg.Builder msg = Msg.newBuilder()
      .addEvents(0, e1)
      .addEvents(1, e2)
      .addEvents(2, e3)
      .addEvents(3, e4)
      .addEvents(4, e5);
    return sendMsg(channel, new ReturnableEvent(msg));
  }

  public ListenableFuture<Boolean> sendEvents(Event... events) throws InterruptedException {
    final Msg.Builder msg = Msg.newBuilder()
      .addAllEvents(asList(events));
    return sendMsg(channel, new ReturnableEvent(msg));
  }

  public ListenableFuture<List<Proto.Event>> query(String query) throws InterruptedException {
    final Msg.Builder msg = Msg.newBuilder()
      .setQuery(Proto.Query.newBuilder()
        .setString(query));
    return sendMsg(channel, new ReturnableQuery(msg));
  }

  private static <T> ListenableFuture<T> sendMsg(Channel channel, ReturnableMessage<T> returnable) throws InterruptedException {
    channel.write(returnable);
    return returnable;
  }

  @Override
  public void close() throws IOException {
    channel.close();
  }

  protected Channel getChannel() {
    return channel;
  }

}
