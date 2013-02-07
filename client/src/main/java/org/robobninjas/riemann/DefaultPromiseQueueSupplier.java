package org.robobninjas.riemann;

import com.google.common.base.Supplier;
import com.google.common.collect.Queues;

import java.util.concurrent.BlockingQueue;

public class DefaultPromiseQueueSupplier implements Supplier<BlockingQueue<ReturnableMessage<?>>> {
  @Override
  public BlockingQueue<ReturnableMessage<?>> get() {
    return Queues.newArrayBlockingQueue(10000);
  }
}
