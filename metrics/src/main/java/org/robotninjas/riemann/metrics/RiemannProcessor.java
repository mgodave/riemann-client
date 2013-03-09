package org.robotninjas.riemann.metrics;

import com.yammer.metrics.core.*;
import org.robotninjas.riemann.client.RiemannConnection;

public class RiemannProcessor implements MetricProcessor<Long> {

  private final RiemannConnection connection;

  public RiemannProcessor(RiemannConnection connection) {
    this.connection = connection;
  }

  @Override
  public void processMeter(MetricName metricName, Metered metered, Long epoch) throws Exception {

  }

  @Override
  public void processCounter(MetricName metricName, Counter counter, Long epoch) throws Exception {

  }

  @Override
  public void processHistogram(MetricName metricName, Histogram histogram, Long epoch) throws Exception {

  }

  @Override
  public void processTimer(MetricName metricName, Timer timer, Long epoch) throws Exception {

  }

  @Override
  public void processGauge(MetricName metricName, Gauge<?> gauge, Long epoch) throws Exception {

  }

}
