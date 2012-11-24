/*

 Copyright 2012 David Rusek

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

*/

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

  public RiemannConnectionPool(Client client, Config config) {
    super(new ConnectionFactory(client), config);
    this.client = client;
  }

  @Override
  public void close() throws Exception {
    super.close();
    client.shutdown();
  }

}
