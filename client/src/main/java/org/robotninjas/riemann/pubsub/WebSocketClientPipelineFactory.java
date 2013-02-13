package org.robotninjas.riemann.pubsub;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;

public class WebSocketClientPipelineFactory implements ChannelPipelineFactory {

  private final WebSocketClientHandshaker handshaker;
  private final QueryResultListener listener;

  public WebSocketClientPipelineFactory(WebSocketClientHandshaker handshaker, QueryResultListener listener) {
    this.handshaker = handshaker;
    this.listener = listener;
  }

  @Override
  public ChannelPipeline getPipeline() throws Exception {

    final ChannelPipeline pipeline = Channels.pipeline();

    pipeline.addLast("http-decoder", new HttpResponseDecoder());
    pipeline.addLast("http-encoder", new HttpRequestEncoder());

    pipeline.addLast("ws-handler", new WebSocketClientHandler(handshaker, listener));

    return pipeline;
  }
}
