package org.robobninjas.riemann;

import com.google.common.base.Supplier;
import com.google.common.collect.Queues;
import org.jboss.netty.channel.MessageEvent;

import java.util.Queue;

public class DefaultMessageQueueSupplier implements Supplier<Queue<MessageEvent>> {
  @Override
  public Queue<MessageEvent> get() {
    return Queues.newConcurrentLinkedQueue();
  }
}
