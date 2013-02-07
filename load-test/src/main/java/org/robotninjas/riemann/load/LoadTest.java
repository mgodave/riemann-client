package org.robotninjas.riemann.load;

import com.aphyr.riemann.Proto;
import com.google.common.base.Supplier;
import com.google.common.net.HostAndPort;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.palominolabs.metrics.guice.InstrumentationModule;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.apache.commons.cli.*;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.Integer.parseInt;

public class LoadTest {

  private static final int NUM_CLIENT_WORKERS = 5;
  private static final int BATCH_SIZE = 400;
  private static final int NUM_CONNECTIONS = 5;
  private static final int NUM_NETTY_WORKERS = 5;
  private static final int BUFFER_SIZE = 16384;

  private final String address;
  private final int port;
  private final int clientWorkers;
  private final int batchSize;
  private final int numConnections;
  private final int numNettyWorkers;
  private final int bufferSize;
  private final Supplier<Proto.Event> eventSupplier;

  public LoadTest(String address, int port) {
    this(address, port, NUM_CLIENT_WORKERS, BATCH_SIZE, NUM_CONNECTIONS,
        NUM_NETTY_WORKERS, BUFFER_SIZE, new DefaultEventSupplier());
  }

  public LoadTest(String address, int port, int clientWorkers) {
    this(address, port, clientWorkers, BATCH_SIZE, NUM_CONNECTIONS,
        NUM_NETTY_WORKERS, BUFFER_SIZE, new DefaultEventSupplier());
  }

  public LoadTest(String address, int port, int clientWorkers, int batchSize, int numConnections,
                  int numNettyWorkers, int bufferSize, Supplier<Proto.Event> eventSupplier) {

    this.address = address;
    this.port = port;
    this.clientWorkers = clientWorkers;
    this.batchSize = batchSize;
    this.numConnections = numConnections;
    this.numNettyWorkers = numNettyWorkers;
    this.bufferSize = bufferSize;
    this.eventSupplier = eventSupplier;
  }

  public void start() {

    final GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
    poolConfig.maxActive = numConnections;

    final Injector injector = Guice.createInjector(
        new InstrumentationModule(),
        new InstrumentedClientModule(address, port, numNettyWorkers, poolConfig, bufferSize),
        new LoadTestModule(clientWorkers, batchSize, eventSupplier));

    final ConsoleReporter consoleReporter = injector.getInstance(ConsoleReporter.class);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        consoleReporter.shutdown();
      }
    });
    consoleReporter.start(1, TimeUnit.SECONDS);

    final LoadTestService loadTestService = injector.getInstance(LoadTestService.class);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        loadTestService.stopAndWait();
      }
    });
    loadTestService.startAndWait();
  }

  public static void main(String[] args) {

    final Options opts = new Options();
    opts.addOption("w", "client-workers", true, "number of client workers");
    opts.addOption("b", "batch-size", true, "number of Events to send in each Msg");
    opts.addOption("c", "connections", true, "number of concurrent connections");
    opts.addOption("n", "netty-workers", true, "number of netty worker threads");
    opts.addOption("s", "buffer-size", true, "netty pipeline buffer size in bytes");

    try {

      final Parser parser = new PosixParser();
      final CommandLine line = parser.parse(opts, args);

      final int clientWorkers = line.hasOption('w') ? parseInt(line.getOptionValue('w')) : NUM_CLIENT_WORKERS;
      final int batchSize = line.hasOption('b') ? parseInt(line.getOptionValue('b')) : BATCH_SIZE;
      final int numConnections = line.hasOption('c') ? parseInt(line.getOptionValue('c')) : NUM_CONNECTIONS;
      final int numNettyWorkers = line.hasOption('n') ? parseInt(line.getOptionValue('n')) : NUM_NETTY_WORKERS;
      final int bufferSize = line.hasOption('s') ? parseInt(line.getOptionValue('s')) : BUFFER_SIZE;

      final List<String> otherArgs = line.getArgList();
      HostAndPort riemannHostAndPort = HostAndPort.fromParts("localhost", 5555);
      if (otherArgs.size() >= 1) {
        riemannHostAndPort = HostAndPort.fromString(otherArgs.get(0));
      }

      final LoadTest loadTest =
          new LoadTest(riemannHostAndPort.getHostText(), riemannHostAndPort.getPort(), clientWorkers,
              batchSize, numConnections, numNettyWorkers, bufferSize, new DefaultEventSupplier());

      loadTest.start();

    } catch (ParseException e) {
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("load-test", opts);
    }


  }

}
