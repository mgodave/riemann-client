package org.robotninjas.riemann.client;

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.ExecutionException;

public interface RiemannConnection extends Closeable {

  void send(Proto.Event event);

  void send(Iterable<Proto.Event> events);

  public ListenableFuture<List<Proto.Event>> query(String query) throws ExecutionException, InterruptedException;

  boolean isOpen();

}
