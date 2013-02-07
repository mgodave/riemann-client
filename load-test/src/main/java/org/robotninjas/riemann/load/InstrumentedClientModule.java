package org.robotninjas.riemann.load;

import com.google.common.base.Supplier;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jboss.netty.channel.MessageEvent;
import org.robobninjas.riemann.ReturnableMessage;
import org.robobninjas.riemann.guice.RiemannClientModule;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class InstrumentedClientModule extends RiemannClientModule{

  public InstrumentedClientModule(String address, int port, int numWorkers, GenericObjectPool.Config poolConfig, int bufferSize) {
    super(address, port, numWorkers, poolConfig, bufferSize);
  }

  @Override
  protected Supplier<BlockingQueue<ReturnableMessage<?>>> internalGetPromiseQueueSupplier() {
    return super.internalGetPromiseQueueSupplier();
  }

  @Override
  protected Supplier<Queue<MessageEvent>> internalGetMessageQueueSupplier() {
    return super.internalGetMessageQueueSupplier();
  }
}
