package org.robotninjas.riemann.load;

import com.google.common.collect.ForwardingConcurrentMap;
import com.google.common.collect.ForwardingQueue;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ForwardingBlockingQueue;
import com.google.inject.Inject;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

public class InstrumentedBlockingQueue<E> extends ForwardingQueue<E> {

  private final BlockingQueue<E> backing = Queues.newArrayBlockingQueue(10000);
  private final Gauge<Integer> sizeGauge;
  private final UUID uuid = UUID.randomUUID();

  @Inject
  public InstrumentedBlockingQueue(MetricsRegistry registy) {
    sizeGauge = registy.newGauge(new MetricName(getClass(), "outstanding-sends", uuid.toString()), new Gauge<Integer>() {
      @Override public Integer value() {
        return size();
      }
    });
  }

  @Override protected BlockingQueue<E> delegate() {
    return backing;
  }

}
