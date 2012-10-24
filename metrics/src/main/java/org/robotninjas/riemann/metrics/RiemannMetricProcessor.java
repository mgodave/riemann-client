package org.robotninjas.riemann.metrics;

import com.yammer.metrics.core.*;
import org.robobninjas.riemann.Connection;

public class RiemannMetricProcessor implements MetricProcessor<Long> {

  private final Connection connection;

  public RiemannMetricProcessor(Connection connection) {
    this.connection = connection;
  }

  @Override
  public void processMeter(MetricName metricName, Metered metered, Long aLong) throws Exception {
  }

  @Override
  public void processCounter(MetricName metricName, Counter counter, Long aLong) throws Exception {
  }

  @Override
  public void processHistogram(MetricName metricName, Histogram histogram, Long aLong) throws Exception {

  }

  @Override
  public void processTimer(MetricName metricName, Timer timer, Long aLong) throws Exception {

  }

  @Override
  public void processGauge(MetricName metricName, Gauge<?> gauge, Long aLong) throws Exception {

  }
}
