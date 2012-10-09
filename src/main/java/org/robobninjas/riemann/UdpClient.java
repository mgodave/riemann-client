package org.robobninjas.riemann;

import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.DatagramChannelFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class UdpClient implements Client {

  private final ConnectionlessBootstrap bootstrap;

  @Inject
  public UdpClient(DatagramChannelFactory channelFactory, String address, int port) {
    bootstrap = getBootstrap(channelFactory);
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
  }

  @Inject
  public UdpClient(DatagramChannelFactory channelFactory, String address) {
    this(channelFactory, address, Clients.DEFAULT_PORT);
  }

  @Inject
  public UdpClient(DatagramChannelFactory channelFactory) {
    bootstrap = getBootstrap(channelFactory);
  }

  private static ConnectionlessBootstrap getBootstrap(DatagramChannelFactory channelFactory) {
    final ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(channelFactory);
    bootstrap.setPipelineFactory(new UdpClientPipelineFactory());
    return bootstrap;
  }

  @Override
  public Connection makeConnection(String address, int port) throws InterruptedException {
    final ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(address, port));
    connectFuture.sync();
    return new Connection(connectFuture.getChannel());
  }

  @Override
  public Connection makeConnection(String address) throws InterruptedException {
    return makeConnection(address, Clients.DEFAULT_PORT);
  }

  @Override
  public Connection makeConnection() throws InterruptedException {
    final ChannelFuture connectFuture = bootstrap.connect();
    connectFuture.sync();
    return new Connection(connectFuture.getChannel());
  }

  @Override
  public void shutdown() {
    bootstrap.releaseExternalResources();
  }

}
