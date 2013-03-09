package org.robotninjas.riemann.pubsub;

import com.google.common.base.Supplier;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;

import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class RiemannPubSub {

  private static ExecutorService getExecutorService() {
    return newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("Riemann RiemannClient Thread")
        .build());
  }

  public static RiemannPubSubClient makeClient(String host, int port) {
    return new RiemannPubSubClient(
      host, port,
      new WebSocketClientHandshakerFactory(),
      new Supplier<ClientBootstrap>() {
        @Override
        public ClientBootstrap get() {
          return new ClientBootstrap(new NioClientSocketChannelFactory(getExecutorService(), getExecutorService()));
        }
      },
      MoreExecutors.sameThreadExecutor());
  }

  public static void main(String[] args) {
    final RiemannPubSubClient client = makeClient("localhost", 5556);
    try {
      final RiemannPubSubConnection connection = client.makeConnection("state = \"ok\"", true, new QueryResultListener() {
        @Override
        public void handleResult(String result) {
          System.out.println(result);
        }
      });
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

}
