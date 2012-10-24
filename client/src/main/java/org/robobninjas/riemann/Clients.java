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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;

import javax.annotation.concurrent.ThreadSafe;

import java.util.concurrent.ExecutorService;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.robobninjas.riemann.Client.DEFAULT_PORT;

@ThreadSafe
public class Clients {

  private static ExecutorService getExecutorService() {
    return newCachedThreadPool(
      new ThreadFactoryBuilder()
        .setDaemon(true)
        .setNameFormat("Riemann Client Thread %1")
        .build());
  }

  public static UdpClient makeUdpClient(String address, int port) {
    checkNotNull(address, "Address cannot be null");
    checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");
    final OioDatagramChannelFactory channelFactory = new OioDatagramChannelFactory(getExecutorService());
    return new UdpClient(channelFactory, address, port);
  }

  public static UdpClient makeUdpClient(String address) {
    return makeUdpClient(address, DEFAULT_PORT);
  }

  public static UdpClient makeUdpClient() {
    return makeUdpClient("localhost");
  }

  public static TcpClient makeTcpClient(String address, int port) {
    checkNotNull(address, "Address cannot be null");
    checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");
    final OioClientSocketChannelFactory channelFactory = new OioClientSocketChannelFactory(getExecutorService());
    return new TcpClient(channelFactory, address, port);
  }

  public static TcpClient makeTcpClient(String address) {
    return makeTcpClient(address, DEFAULT_PORT);
  }

  public static TcpClient makeTcpClient() {
    return makeTcpClient("localhost");
  }

}
