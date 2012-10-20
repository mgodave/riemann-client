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

package org.robotninjas.riemann.sample;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import org.jboss.netty.channel.socket.DatagramChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.robobninjas.riemann.Client;
import org.robobninjas.riemann.UdpClient;
import org.robotninjas.riemann.pool.RiemannConnectionPool;

import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class ClientModule extends AbstractModule {

  private final String address;
  private final int port;

  public ClientModule(String address, int port) {
    this.address = address;
    this.port = port;
  }

  @Override
  protected void configure() {
    try {

      install(new FactoryModuleBuilder()
        .implement(Client.class, UdpClient.class)
        .build(ClientFactory.class));

      bind(DatagramChannelFactory.class)
        .toConstructor(NioDatagramChannelFactory.class
          .getConstructor(Executor.class));

      bind(Executor.class)
        .toInstance(newCachedThreadPool());

      bind(RiemannConnectionPool.class);

    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Provides
  public Client getClient(ClientFactory factory) {
    return factory.create(address, port);
  }

}
