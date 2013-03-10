package org.robotninjas.riemann.load;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.robobninjas.riemann.guice.RiemannClientModule;
import org.robotninjas.riemann.client.ReturnableMessage;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class InstrumentedClientModule extends RiemannClientModule {

  public InstrumentedClientModule(String address, int port, int numWorkers, GenericObjectPool.Config poolConfig) {
    super(address, port, numWorkers, poolConfig);
  }

  @Override
  protected void bindOutstandingMessagesQueue(Key<BlockingQueue<ReturnableMessage>> key) {
    bind(key).to(new TypeLiteral<InstrumentedBlockingQueue<ReturnableMessage>>() {
    });
  }

  @Override
  protected void bindSendBufferQueue(Key<Queue<MessageEvent>> key) {
    bind(key).to(new TypeLiteral<InstrumentedQueue<MessageEvent>>() {
    });
  }

  @Override
  protected void configureBootstrap(ClientBootstrap bootstrap) {
    bootstrap.setOption("writeBufferHighWaterMark", 65536 * 2);
    bootstrap.setOption("writeBufferLowWaterMark", 65536 );
    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("child.tcpNoDelay", true);
  }
}
