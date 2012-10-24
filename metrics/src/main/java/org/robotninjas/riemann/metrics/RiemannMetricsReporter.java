package org.robotninjas.riemann.metrics;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import org.robobninjas.riemann.Client;
import org.robobninjas.riemann.Clients;

import java.io.IOException;

public class RiemannMetricsReporter extends AbstractPollingReporter {

  private final Client client;
  protected final String prefix;
  protected final MetricPredicate predicate;
  protected final Clock clock;
  protected final VirtualMachineMetrics vm;
  public boolean printVMMetrics = true;


  /**
   * Creates a new {@link RiemannMetricsReporter}.
   *
   * @param host   is graphite server
   * @param port   is port on which graphite server is running
   * @param prefix is prepended to all names reported to graphite
   * @throws IOException if there is an error connecting to the Graphite server
   */
  public RiemannMetricsReporter(String host, int port, String prefix) throws IOException {
    this(Metrics.defaultRegistry(), host, port, prefix);
  }

  /**
   * Creates a new {@link RiemannMetricsReporter}.
   *
   * @param metricsRegistry the metrics registry
   * @param host            is graphite server
   * @param port            is port on which graphite server is running
   * @param prefix          is prepended to all names reported to graphite
   * @throws IOException if there is an error connecting to the Graphite server
   */
  public RiemannMetricsReporter(MetricsRegistry metricsRegistry, String host, int port, String prefix) throws IOException {
    this(metricsRegistry,
      prefix,
      MetricPredicate.ALL,
      Clients.makeTcpClient(host, port),
      Clock.defaultClock());
  }

  /**
   * Creates a new {@link RiemannMetricsReporter}.
   *
   * @param metricsRegistry the metrics registry
   * @param prefix          is prepended to all names reported to graphite
   * @param predicate       filters metrics to be reported
   * @param client  a {@link Client} instance
   * @param clock           a {@link Clock} instance
   * @throws IOException if there is an error connecting to the Graphite server
   */
  public RiemannMetricsReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, Client client, Clock clock) throws IOException {
    this(metricsRegistry, prefix, predicate, client, clock,
      VirtualMachineMetrics.getInstance());
  }

  /**
   * Creates a new {@link RiemannMetricsReporter}.
   *
   * @param metricsRegistry the metrics registry
   * @param prefix          is prepended to all names reported to graphite
   * @param predicate       filters metrics to be reported
   * @param client          a {@link Client} instance
   * @param clock           a {@link Clock} instance
   * @param vm              a {@link VirtualMachineMetrics} instance
   * @throws IOException if there is an error connecting to the Graphite server
   */
  public RiemannMetricsReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, Client client, Clock clock, VirtualMachineMetrics vm) throws IOException {
    this(metricsRegistry, prefix, predicate, client, clock, vm, "graphite-reporter");
  }

  /**
   * Creates a new {@link RiemannMetricsReporter}.
   *
   * @param metricsRegistry the metrics registry
   * @param prefix          is prepended to all names reported to graphite
   * @param predicate       filters metrics to be reported
   * @param client          a {@link Client} instance
   * @param clock           a {@link Clock} instance
   * @param vm              a {@link VirtualMachineMetrics} instance
   * @throws IOException if there is an error connecting to the Graphite server
   */
  public RiemannMetricsReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, Client client, Clock clock, VirtualMachineMetrics vm, String name) throws IOException {
    super(metricsRegistry, name);
    this.client = client;
    this.vm = vm;

    this.clock = clock;

    if (prefix != null) {
      // Pre-append the "." so that we don't need to make anything conditional later.
      this.prefix = prefix + ".";
    } else {
      this.prefix = "";
    }
    this.predicate = predicate;
  }

  @Override
  public void run() {
    Socket socket = null;
    try {
      socket = this.socketProvider.get();
      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      final long epoch = clock.getTime() / 1000;
      if (this.printVMMetrics) {
        printVmMetrics(epoch);
      }
      printRegularMetrics(epoch);
      writer.flush();
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Error writing to Graphite", e);
      } else {
        LOG.warn("Error writing to Graphite: {}", e.getMessage());
      }
      if (writer != null) {
        try {
          writer.flush();
        } catch (IOException e1) {
          LOG.error("Error while flushing writer:", e1);
        }
      }
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (IOException e) {
          LOG.error("Error while closing socket:", e);
        }
      }
      writer = null;
    }
  }
}
