package org.robobninjas.riemann.guice;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.robobninjas.riemann.RiemannClient;
import org.robobninjas.riemann.TcpClientPipelineFactory;
import org.robobninjas.riemann.TcpRiemannClient;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiemannClientModule extends PrivateModule {

  private final String address;
  private final int port;
  private final int numWorkers;
  private final GenericObjectPool.Config poolConfig;

  public RiemannClientModule(String address, int port, int numWorkers, GenericObjectPool.Config poolConfig) {
    this.address = address;
    this.port = port;
    this.numWorkers = numWorkers;
    this.poolConfig = poolConfig;
  }

  @Override
  protected void configure() {
    bind(RiemannClient.class).to(TcpRiemannClient.class);
    expose(RiemannClient.class);
  }

  @Provides
  @Exposed
  @Singleton
  public RiemannConnectionPool getConnectionPool(RiemannClient client) {
    final RiemannConnectionPool pool = new RiemannConnectionPool(client, poolConfig);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          pool.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
    return pool;
  }

  @Provides
  @Singleton
  @NettyExecutor
  public Executor getExecutor() {
    final ExecutorService executor = Executors.newCachedThreadPool();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        executor.shutdown();
      }
    });
    return executor;
  }

  @Provides
  @Singleton
  public NioClientBossPool getBossPool(@NettyExecutor Executor executor) {
    final NioClientBossPool bossPool = new NioClientBossPool(executor, 1);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        bossPool.shutdown();
      }
    });
    return bossPool;
  }

  @Provides
  @Singleton
  public NioWorkerPool getWorkerPool(@NettyExecutor Executor executor) {
    final NioWorkerPool workerPool = new NioWorkerPool(executor, numWorkers);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        workerPool.shutdown();
      }
    });
    return workerPool;
  }

  @Provides
  public ClientBootstrap getClientBootstrap(NioClientBossPool boss, NioWorkerPool worker) {
    final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(boss, worker);
    final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setPipelineFactory(new TcpClientPipelineFactory());
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("child.tcpNoDelay", true);
    return bootstrap;
  }

}
