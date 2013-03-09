package org.robotninjas.riemann.metrics;

import com.google.common.io.Closeables;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import org.robotninjas.riemann.client.Clients;
import org.robotninjas.riemann.client.RiemannClient;
import org.robotninjas.riemann.client.RiemannConnection;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;

public class RiemannReporter extends AbstractPollingReporter {

  public static final String REPORTER_NAME = "RiemannReporter";

  private final RiemannClient client;
  private final Clock clock;

  public RiemannReporter() {
    this(Metrics.defaultRegistry(), Clients.makeClient(), Clock.defaultClock());
  }

  public RiemannReporter(MetricsRegistry registry, RiemannClient client, Clock clock) {
    super(registry, REPORTER_NAME);
    this.client = client;
    this.clock = clock;
  }

  @Override
  public void run() {
    RiemannConnection conn = null;
    try {
      conn = client.makeConnection();
      final long epoch = clock.time() / 1000;
      for (Map.Entry<String, SortedMap<MetricName, Metric>> entry : getMetricsRegistry().getGroupedMetrics(predicate).entrySet()) {
        for (Map.Entry<MetricName, Metric> subEntry : entry.getValue().entrySet()) {
          final Metric metric = subEntry.getValue();
          if (metric != null) {
            try {
              final
              dispatcher.dispatch(subEntry.getValue(), subEntry.getKey(), this, epoch);
            } catch (Exception ignored) {
              LOG.error("Error printing regular metrics:", ignored);
            }
          }
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } finally {
      try {
        Closeables.close(conn, false);
      } catch (IOException e) {
      }
    }

  }

}
