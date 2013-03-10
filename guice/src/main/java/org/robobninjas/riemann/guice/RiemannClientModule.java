package org.robobninjas.riemann.guice;

import com.google.common.base.Supplier;
import com.google.common.collect.Queues;
import com.google.inject.*;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.socket.nio.NioClientBossPool;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioWorkerPool;
import org.robotninjas.riemann.client.ReturnableMessage;
import org.robotninjas.riemann.client.RiemannTcpClient;
import org.robotninjas.riemann.client.TcpClientPipelineFactory;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.net.InetSocketAddress;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RiemannClientModule extends PrivateModule {

  private static final GenericObjectPool.Config DEFAULT_CONFIG = new GenericObjectPool.Config();
  private static final int DEFAULT_WORKERS = 1;

  static {
    DEFAULT_CONFIG.maxActive = 1;
  }

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

  public RiemannClientModule(String address, int port) {
    this(address, port, DEFAULT_WORKERS, DEFAULT_CONFIG);
  }

  @Override
  protected void configure() {
    bind(RiemannTcpClient.class);
    expose(RiemannTcpClient.class);
    bind(TcpClientPipelineFactory.class);
    bindSendBufferQueue(Key.get(new TypeLiteral<Queue<MessageEvent>>() {
    }));
    bindOutstandingMessagesQueue(Key.get(new TypeLiteral<BlockingQueue<ReturnableMessage>>() {
    }));
  }

  @Provides
  @Exposed
  @Singleton
  public RiemannConnectionPool getConnectionPool(RiemannTcpClient client) {
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
  public ClientBootstrap getClientBootstrap(NioClientBossPool boss, NioWorkerPool worker, TcpClientPipelineFactory pipelineFactory) {
    final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(boss, worker);
    final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setPipelineFactory(pipelineFactory);
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("child.tcpNoDelay", true);
    configureBootstrap(bootstrap);
    return bootstrap;
  }

  @Provides
  public Supplier<BlockingQueue<ReturnableMessage>> getPromiseQueueSupplier(final Provider<BlockingQueue<ReturnableMessage>> provider) {
    return new Supplier<BlockingQueue<ReturnableMessage>>() {
      @Override
      public BlockingQueue<ReturnableMessage> get() {
        return provider.get();
      }
    };
  }

  @Provides
  public Supplier<Queue<MessageEvent>> getSendBufferQueueSupplier(final Provider<Queue<MessageEvent>> provider) {
    return new Supplier<Queue<MessageEvent>>() {
      @Override
      public Queue<MessageEvent> get() {
        return provider.get();
      }
    };
  }

  protected void bindOutstandingMessagesQueue(Key<BlockingQueue<ReturnableMessage>> key) {
    bind(key).toProvider(new Provider<BlockingQueue<ReturnableMessage>>() {
      @Override
      public BlockingQueue<ReturnableMessage> get() {
        return Queues.newArrayBlockingQueue(10000);
      }
    });
  }

  protected void bindSendBufferQueue(Key<Queue<MessageEvent>> key) {
    bind(key).toProvider(new Provider<Queue<MessageEvent>>() {
      @Override
      public Queue<MessageEvent> get() {
        return Queues.newConcurrentLinkedQueue();
      }
    });
  }

  protected void configureBootstrap(ClientBootstrap bootstrap) {

  }


}
