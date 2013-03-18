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

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.robotninjas.riemann.client.RiemannTcpConnection;
import org.robotninjas.riemann.client.RiemannTcpClient;

class ConnectionFactory extends BasePoolableObjectFactory<RiemannTcpConnection> {

  private final RiemannTcpClient client;

  public ConnectionFactory(RiemannTcpClient client) {
    this.client = client;
  }

  @Override
  public RiemannTcpConnection makeObject() throws Exception {
    return client.makeConnection();
  }

  @Override
  public boolean validateObject(RiemannTcpConnection obj) {
    return super.validateObject(obj);
  }
}
