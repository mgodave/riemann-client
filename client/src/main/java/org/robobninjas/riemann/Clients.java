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

import com.aphyr.riemann.Proto;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.robobninjas.riemann.RiemannClient.DEFAULT_PORT;

@ThreadSafe
public class Clients {

  private static ExecutorService getExecutorService() {
    return newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("Riemann RiemannClient Thread")
        .build());
  }

  public static TcpRiemannClient makeClient(String address, int port) {
    checkNotNull(address, "Address cannot be null");
    checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");
    final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(getExecutorService(), getExecutorService(), 1, 1);
    final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
    bootstrap.setPipelineFactory(new TcpClientPipelineFactory(8192));
    bootstrap.setOption("remoteAddress", new InetSocketAddress(address, port));
    bootstrap.setOption("tcpNoDelay", true);
    bootstrap.setOption("child.tcpNoDelay", true);
    return new TcpRiemannClient(bootstrap);
  }

  public static TcpRiemannClient makeClient(String address) {
    return makeClient(address, DEFAULT_PORT);
  }

  public static TcpRiemannClient makeClient() {
    return makeClient("localhost");
  }

//  public static TcpRiemannClient makeClient(URI uri) {
//    final NioClientSocketChannelFactory channelFactory = new NioClientSocketChannelFactory(getExecutorService(), getExecutorService(), 1, 1);
//    final ClientBootstrap bootstrap = new ClientBootstrap(channelFactory);
//    bootstrap.setPipelineFactory(new WebSocketClientPipelineFactory(8192, uri));
//    bootstrap.setOption("remoteAddress", new InetSocketAddress(uri.getHost(), uri.getPort()));
//    bootstrap.setOption("tcpNoDelay", true);
//    bootstrap.setOption("child.tcpNoDelay", true);
//    return new TcpRiemannClient(bootstrap);
//  }
//
//  public static void main(String[] args) throws InterruptedException {
//    RiemannConnection conn = Clients.makeClient(URI.create("ws://localhost:5556/index?query=*")).makeConnection();
//  }

}
