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

package org.robobninjas.riemann;

import com.google.inject.assistedinject.Assisted;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.DatagramChannelFactory;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import java.net.InetSocketAddress;

import static org.robobninjas.riemann.Client.DEFAULT_PORT;

@ThreadSafe
public class UdpClient {

  private final ConnectionlessBootstrap bootstrap;

  @Inject
  public UdpClient(DatagramChannelFactory channelFactory, @Assisted String address, @Assisted int port) {
    bootstrap = getBootstrap(channelFactory);
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
  }

  public UdpClient(DatagramChannelFactory channelFactory, String address) {
    this(channelFactory, address, Client.DEFAULT_PORT);
  }

  public UdpClient(DatagramChannelFactory channelFactory) {
    bootstrap = getBootstrap(channelFactory);
  }

  private static ConnectionlessBootstrap getBootstrap(DatagramChannelFactory channelFactory) {
    final ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(channelFactory);
    bootstrap.setPipelineFactory(new UdpClientPipelineFactory());
    return bootstrap;
  }

  public Connection makeConnection(String address, int port) throws InterruptedException {
    final ChannelFuture connectFuture = bootstrap.connect(new InetSocketAddress(address, port));
    connectFuture.sync();
    return new Connection(connectFuture.getChannel());
  }

  public Connection makeConnection(String address) throws InterruptedException {
    return makeConnection(address, Client.DEFAULT_PORT);
  }

  public Connection makeConnection() throws InterruptedException {
    final ChannelFuture connectFuture = bootstrap.connect();
    connectFuture.sync();
    return new Connection(connectFuture.getChannel());
  }

  public void shutdown() {
    bootstrap.releaseExternalResources();
  }

}
