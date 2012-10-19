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
import java.util.concurrent.Executors;

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
          .getConstructor(
            Executor.class));

      bind(Executor.class)
        .toInstance(Executors.newCachedThreadPool());

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
