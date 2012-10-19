package org.robotninjas.riemann.pool;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.robobninjas.riemann.Client;
import org.robobninjas.riemann.Connection;

class ConnectionFactory extends BasePoolableObjectFactory<Connection> {

  private final Client client;

  public ConnectionFactory(Client client) {
    this.client = client;
  }

  @Override
  public Connection makeObject() throws Exception {
    return client.makeConnection();
  }
}
