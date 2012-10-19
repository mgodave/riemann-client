package org.robotninjas.riemann.pool;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.robobninjas.riemann.Client;
import org.robobninjas.riemann.Connection;

import javax.inject.Inject;

public class RiemannConnectionPool extends GenericObjectPool<Connection> {

  @Inject
  public RiemannConnectionPool(Client client) {
    super(new ConnectionFactory(client), 10);
  }

}
