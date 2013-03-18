package org.robotninjas.riemann.client;

import com.google.common.base.Optional;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelFuture;

import java.net.InetSocketAddress;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.base.Throwables.propagateIfInstanceOf;

public class RiemannUdpClient implements RiemannClient {

  private final ConnectionlessBootstrap bootstrap;
  private final int maxRetries = 5;

  public RiemannUdpClient(ConnectionlessBootstrap bootstrap) {
    this.bootstrap = bootstrap;
  }

  @Override
  public RiemannUdpConnection makeConnection() throws InterruptedException {

    Optional<Exception> lastException = Optional.absent();
    for (int i = 0; i <= maxRetries; ++i) {
      try {
        final ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.sync();
        return new RiemannUdpConnection(channelFuture.getChannel(), bootstrap);
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
  public RiemannUdpConnection makeConnection(String address, int port) throws InterruptedException {
    Optional<Exception> lastException = Optional.absent();
    for (int i = 0; i <= maxRetries; ++i) {
      try {
        final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(address, port));
        channelFuture.sync();
        return new RiemannUdpConnection(channelFuture.getChannel(), bootstrap);
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
  public RiemannUdpConnection makeConnection(String address) throws InterruptedException {
    return makeConnection(address, DEFAULT_PORT);
  }

  @Override
  public void shutdown() {
    bootstrap.releaseExternalResources();
    bootstrap.shutdown();
  }

}
