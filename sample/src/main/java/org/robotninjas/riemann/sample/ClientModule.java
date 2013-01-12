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
import org.robobninjas.riemann.*;
import org.robobninjas.riemann.RiemannClient;
import org.robotninjas.riemann.pool.RiemannConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.concurrent.Executor;

import static java.util.concurrent.Executors.newCachedThreadPool;

public class ClientModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClientModule.class);

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
        .implement(RiemannClient.class, TcpRiemannClient.class)
        .build(ClientFactory.class));

      bind(DatagramChannelFactory.class)
        .toConstructor(NioDatagramChannelFactory.class
          .getConstructor(Executor.class));

      bind(Executor.class)
        .toInstance(newCachedThreadPool());

    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
  }

  @Provides
  public RiemannClient getClient(ClientFactory factory) {
    return factory.create(address, port);
  }

  @Provides
  @Singleton
  public RiemannConnectionPool getConnectionPool(org.robobninjas.riemann.RiemannClient client) {
    final RiemannConnectionPool connectionPool = new RiemannConnectionPool(client);
    Runtime.getRuntime().addShutdownHook(
      new Thread(
        new Runnable() {
          @Override
          public void run() {
            try {
              connectionPool.close();
            } catch (Exception e) {
              LOGGER.info("Exception thrown while closing connection pool", e);
            }
          }
        }
      )
    );
    return connectionPool;
  }

}
