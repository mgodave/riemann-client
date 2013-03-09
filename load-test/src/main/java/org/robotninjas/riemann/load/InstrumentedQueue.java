package org.robotninjas.riemann.load;

import com.google.common.collect.ForwardingQueue;
import com.google.common.collect.Queues;
import com.google.inject.Inject;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

import java.util.Queue;
import java.util.UUID;

public class InstrumentedQueue<E> extends ForwardingQueue<E> {

  private final Queue<E> backing = Queues.newConcurrentLinkedQueue();
  private final Gauge<Integer> sizeGauge;
  private final UUID uuid = UUID.randomUUID();

  @Inject
  public InstrumentedQueue(MetricsRegistry registy) {
    sizeGauge = registy.newGauge(new MetricName(getClass(), "buffered-sends", uuid.toString()), new Gauge<Integer>() {
      @Override
      public Integer value() {
        return size();
      }
    });
  }

  @Override
  protected Queue<E> delegate() {
    return backing;
  }

}
