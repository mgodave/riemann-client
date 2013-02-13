package org.robobninjas.riemann;

import com.google.common.base.Supplier;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.URI;
import java.util.Queue;

public class WebSocketClientPipelineFactory extends TcpClientPipelineFactory {

  private final URI uri;

  @Inject
  public WebSocketClientPipelineFactory(Supplier<Queue<ReturnableMessage>> promiseQueueSupplier,
                                        Supplier<Queue<MessageEvent>> messageQueueSupplier,
                                        @BufferSize int bufferSize, URI uri) {

    super(promiseQueueSupplier, messageQueueSupplier, bufferSize);
    this.uri = uri;

  }

  public WebSocketClientPipelineFactory(int bufferSize, URI uri) {
    this(
        new Supplier<Queue<ReturnableMessage>>() {
          @Override public Queue<ReturnableMessage> get() {
            return Queues.newConcurrentLinkedQueue();
          }
        },
        new Supplier<Queue<MessageEvent>>() {
          @Override public Queue<MessageEvent> get() {
            return Queues.newConcurrentLinkedQueue();
          }
        },
        bufferSize, uri);
  }

  @Override public ChannelPipeline getPipeline() throws Exception {

    final ChannelPipeline pipeline = super.getPipeline();

    pipeline.addLast("http-decoder", new HttpResponseDecoder());
    pipeline.addLast("http-encoder", new HttpRequestEncoder());

    final WebSocketClientHandshaker handshaker = new WebSocketClientHandshakerFactory()
        .newHandshaker(uri, WebSocketVersion.UNKNOWN.V13, null, false, null);

    pipeline.addLast("ws-handler", new WebSocketClientHandler(handshaker));

    return pipeline;
  }
}
