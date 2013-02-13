package org.robotninjas.riemann.pubsub;

import com.google.common.base.Supplier;
import com.google.inject.Inject;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executor;

public class RiemannPubSubClient {

  private final WebSocketClientHandshakerFactory handshakerFactory;
  private final Supplier<ClientBootstrap> bootstrapSupplier;
  private final Executor resultExecutor;
  private final URI baseUri;

  @Inject
  public RiemannPubSubClient(URI baseUri, WebSocketClientHandshakerFactory handshakerFactory, Supplier<ClientBootstrap> bootstrapSupplier, Executor resultExecutor) {
    this.baseUri = baseUri;
    this.handshakerFactory = handshakerFactory;
    this.bootstrapSupplier = bootstrapSupplier;
    this.resultExecutor = resultExecutor;
  }

  public RiemannPubSubConnection makeConnection(String query, boolean subscribe) throws InterruptedException {
    final URI uri = URI.create(baseUri.toString() + "/?query=" + query + (subscribe ? "&subscribe=true" : ""));
    final WebSocketClientHandshaker handshaker = handshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, null);
    final ClientBootstrap bootstrap = bootstrapSupplier.get();
    final RiemannPubSubConnection connection = new RiemannPubSubConnection(resultExecutor);
    bootstrap.setPipelineFactory(new WebSocketClientPipelineFactory(handshaker, connection));
    final ChannelFuture connect = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
    connect.sync();
    connection.init(connect.getChannel());
    connect.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
      @Override public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isDone() && future.isSuccess()) {
          bootstrap.releaseExternalResources();
        }
      }
    });
    return connection;
  }

  public RiemannPubSubConnection makeConnection(String query) throws InterruptedException {
    return makeConnection(query, false);
  }

}
