package org.robotninjas.riemann.pool;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.robobninjas.riemann.Client;
import org.robobninjas.riemann.Connection;

import javax.inject.Inject;

public class RiemannConnectionPool extends GenericObjectPool<Connection> {

  private final Client client;

  @Inject
  public RiemannConnectionPool(Client client) {
    super(new ConnectionFactory(client), 10);
    this.client = client;
  }

  @Override
  public void close() throws Exception {
    super.close();
    client.shutdown();
  }

}
