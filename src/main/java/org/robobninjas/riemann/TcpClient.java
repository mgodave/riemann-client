package org.robobninjas.riemann;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class TcpClient implements Client {

  private final ClientBootstrap bootstrap;

  @Inject
  public TcpClient(ClientSocketChannelFactory channelFactory, String address, int port) {
    bootstrap = getBootstrap(channelFactory);
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
  }

  @Inject
  public TcpClient(ClientSocketChannelFactory channelFactory, String address) {
    this(channelFactory, address, Clients.DEFAULT_PORT);
  }

  @Inject
  public TcpClient(ClientSocketChannelFactory channelFactory) {
    bootstrap = getBootstrap(channelFactory);
  }

  private static ClientBootstrap getBootstrap(ClientSocketChannelFactory channelFactory) {
    final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setPipelineFactory(new TcpClientPipelineFactory());
    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("child.tcpNoDelay", true);
    return bootstrap;
  }

  @Override
  public Connection makeConnection(String address, int port) throws InterruptedException {
    final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(address, port));
    channelFuture.sync();
    return new Connection(channelFuture.getChannel());
  }

  @Override
  public Connection makeConnection(String address) throws InterruptedException {
    return makeConnection(address, Clients.DEFAULT_PORT);
  }

  @Override
  public Connection makeConnection() throws InterruptedException {
    final ChannelFuture channelFuture = bootstrap.connect();
    channelFuture.sync();
    return new Connection(channelFuture.getChannel());
  }

  @Override
  public void shutdown() {
    bootstrap.releaseExternalResources();
  }

}
