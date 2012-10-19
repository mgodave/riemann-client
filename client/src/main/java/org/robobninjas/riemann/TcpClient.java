package org.robobninjas.riemann;

import com.google.common.base.Optional;
import com.google.inject.assistedinject.Assisted;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.ClientSocketChannelFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.net.InetSocketAddress;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.base.Throwables.propagateIfInstanceOf;

@ThreadSafe
public class TcpClient implements Client {

  private static final int DEFAULT_RETRIES = 0;

  private final ClientBootstrap bootstrap;
  private volatile int maxRetries = DEFAULT_RETRIES;

  @Inject
  public TcpClient(ClientSocketChannelFactory channelFactory, @Assisted String address, @Assisted int port) {
    bootstrap = getBootstrap(channelFactory);
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
  }

  public TcpClient(ClientSocketChannelFactory channelFactory, String address) {
    this(channelFactory, address, Client.DEFAULT_PORT);
  }

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

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  @Override
  public Connection makeConnection(String address, int port) throws InterruptedException {

    Optional<Exception> lastException = Optional.absent();
    for (int i = 0; i <= maxRetries; ++i) {
      try {
        final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(address, port));
        channelFuture.sync();
        return new Connection(channelFuture.getChannel());
      } catch (Exception e) {
        propagateIfInstanceOf(e, InterruptedException.class);
        lastException = Optional.of(e);
      }
    }

    if (lastException.isPresent()) {
      throw propagate(lastException.get());
    }

    return null;
  }

  @Override
  public Connection makeConnection(String address) throws InterruptedException {
    return makeConnection(address, DEFAULT_PORT);
  }

  @Override
  public Connection makeConnection() throws InterruptedException {

    Optional<Exception> lastException = Optional.absent();
    for (int i = 0; i <= maxRetries; ++i) {
      try {
        final ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.sync();
        return new Connection(channelFuture.getChannel());
      } catch (Exception e) {
        propagateIfInstanceOf(e, InterruptedException.class);
        lastException = Optional.of(e);
      }
    }

    if (lastException.isPresent()) {
      throw propagate(lastException.get());
    }

    return null;
  }

  @Override
  public void shutdown() {
    bootstrap.releaseExternalResources();
  }

}
