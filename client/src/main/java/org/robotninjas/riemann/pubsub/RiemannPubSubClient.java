package org.robotninjas.riemann.pubsub;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.Executor;

public class RiemannPubSubClient {

  private final WebSocketClientHandshakerFactory handshakerFactory;
  private final Supplier<ClientBootstrap> bootstrapSupplier;
  private final Executor resultExecutor;
  private final String address;
  private final int port;

  @Inject
  public RiemannPubSubClient(String address, int port, WebSocketClientHandshakerFactory handshakerFactory, Supplier<ClientBootstrap> bootstrapSupplier, Executor resultExecutor) {
    this.address = address;
    this.port = port;
    this.handshakerFactory = handshakerFactory;
    this.bootstrapSupplier = bootstrapSupplier;
    this.resultExecutor = resultExecutor;
  }

  public RiemannPubSubConnection makeConnection(String query, boolean subscribe, QueryResultListener listener) throws InterruptedException, URISyntaxException {
    try {
      final String encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.toString());
      final String queryString = "query=" + encodedQuery + (subscribe ? "&subscribe=true" : "");
      final URI uri = new URI("ws", "", address, port, "/index/", queryString, "");
      final WebSocketClientHandshaker handshaker = handshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, null);
      final ClientBootstrap bootstrap = bootstrapSupplier.get();
      final RiemannPubSubConnection connection = new RiemannPubSubConnection(resultExecutor, listener);
      bootstrap.setPipelineFactory(new WebSocketClientPipelineFactory(handshaker, connection));
      final ChannelFuture connect = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
      connect.sync();
      connection.init(connect.getChannel());
      connect.getChannel().getCloseFuture().addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
          if (future.isDone() && future.isSuccess()) {
            bootstrap.releaseExternalResources();
          }
        }
      });
      return connection;
    } catch (UnsupportedEncodingException e) {
      throw Throwables.propagate(e);
    }
  }

  public RiemannPubSubConnection makeConnection(String host, int port, String query, QueryResultListener listener) throws InterruptedException, URISyntaxException {
    return makeConnection(query, false, listener);
  }

}
