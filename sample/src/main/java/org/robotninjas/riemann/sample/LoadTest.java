package org.robotninjas.riemann.sample;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.yammer.metrics.reporting.ConsoleReporter;
import org.apache.commons.cli.*;
import org.robobninjas.riemann.guice.RiemannClientModule;

import java.util.concurrent.TimeUnit;

public class LoadTest {

  private static final int WORKER_COUNT = 1;

  public static void main(String[] args) {

    try {
      final Options options = new Options();
      options.addOption("w", "workers", true, "number of concurrent workers");

      final CommandLineParser parser = new PosixParser();
      final CommandLine line = parser.parse(options, args);

      final int workers = line.hasOption("W") ? ((Number) line.getOptionObject("W")).intValue() : WORKER_COUNT;

      final Injector injector = Guice.createInjector(
          new RiemannClientModule("localhost", 5555, 8192),
          new LoadTestModule(workers));

      final ConsoleReporter consoleReporter = injector.getInstance(ConsoleReporter.class);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override public void run() {
          consoleReporter.shutdown();
        }
      });
      consoleReporter.start(1, TimeUnit.SECONDS);

      final LoadTestService loadTestService = injector.getInstance(LoadTestService.class);
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          loadTestService.stopAndWait();
        }
      });
      loadTestService.startAndWait();

    } catch (ParseException e) {
      e.printStackTrace();
    }

  }

}
