package org.robotninjas.riemann.load;

import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.robotninjas.riemann.client.ReturnableMessage;
import org.robobninjas.riemann.guice.RiemannClientModule;

import java.util.Queue;

public class InstrumentedClientModule extends RiemannClientModule {

  public InstrumentedClientModule(String address, int port, int numWorkers, GenericObjectPool.Config poolConfig, int bufferSize) {
    super(address, port, numWorkers, poolConfig, bufferSize);
  }

  @Override protected void bindOutstandingMessagesQueue(Key<Queue<ReturnableMessage>> key) {
    bind(key).to(new TypeLiteral<InstrumentedBlockingQueue<ReturnableMessage>>() {
    });
  }

}
