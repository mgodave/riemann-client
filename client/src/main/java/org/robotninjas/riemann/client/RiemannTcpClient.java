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

package org.robotninjas.riemann.client;

import com.google.common.base.Optional;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.net.InetSocketAddress;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.base.Throwables.propagateIfInstanceOf;

@ThreadSafe
public class RiemannTcpClient implements RiemannClient {

  private static final int DEFAULT_RETRIES = 0;

  private final ClientBootstrap bootstrap;
  private volatile int maxRetries = DEFAULT_RETRIES;

  @Inject
  public RiemannTcpClient(ClientBootstrap bootstrap) {
    this.bootstrap = bootstrap;
  }

  public int getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(int maxRetries) {
    this.maxRetries = maxRetries;
  }

  @Override
  public AsyncRiemannConnection makeConnection(String address, int port) throws InterruptedException {

    Optional<Exception> lastException = Optional.absent();
    for (int i = 0; i <= maxRetries; ++i) {
      try {
        final ChannelFuture channelFuture = bootstrap.connect(new InetSocketAddress(address, port));
        channelFuture.sync();
        return new AsyncRiemannConnection(channelFuture.getChannel(), bootstrap);
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
  public AsyncRiemannConnection makeConnection(String address) throws InterruptedException {
    return makeConnection(address, DEFAULT_PORT);
  }

  @Override
  public AsyncRiemannConnection makeConnection() throws InterruptedException {

    Optional<Exception> lastException = Optional.absent();
    for (int i = 0; i <= maxRetries; ++i) {
      try {
        final ChannelFuture channelFuture = bootstrap.connect();
        channelFuture.sync();
        return new AsyncRiemannConnection(channelFuture.getChannel(), bootstrap);
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
    bootstrap.shutdown();
  }

}
