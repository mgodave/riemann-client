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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.Executors.newCachedThreadPool;

@ThreadSafe
public class Clients {

  private static ExecutorService getExecutorService() {
    return newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("Riemann RiemannClient Thread")
        .build());
  }

  public static RiemannTcpClient makeTcpClient(String address, int port) {
    checkNotNull(address, "Address cannot be null");
    checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");
    final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(getExecutorService(), getExecutorService(), 1, 1);
    final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setPipelineFactory(new TcpClientPipelineFactory());
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("child.tcpNoDelay", true);
    return new RiemannTcpClient(bootstrap);
  }

  public static RiemannTcpClient makeTcpClient(String address) {
    return makeTcpClient(address, RiemannClient.DEFAULT_PORT);
  }

  public static RiemannTcpClient makeTcpClient() {
    return makeTcpClient("localhost");
  }

  public static RiemannUdpClient makeUdpClient(String address, int port) {
    checkNotNull(address, "Address cannot be null");
    checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");
    final NioDatagramChannelFactory channelFactory = new NioDatagramChannelFactory(getExecutorService());
    final ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(channelFactory);
    bootstrap.setPipelineFactory(new TcpClientPipelineFactory());
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
    return new RiemannUdpClient(bootstrap);
  }

  public static RiemannUdpClient makeUdpClient(String address) {
    return makeUdpClient(address, RiemannClient.DEFAULT_PORT);
  }

  public static RiemannUdpClient makeUdpClient() {
    return makeUdpClient("localhost");
  }


}
