package org.robobninjas.riemann;

import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;

import javax.annotation.concurrent.ThreadSafe;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.robobninjas.riemann.Client.DEFAULT_PORT;

@ThreadSafe
public class Clients {

  public static UdpClient makeUdpClient(String address, int port) {
    checkNotNull(address, "Address cannot be null");
    checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");
    final OioDatagramChannelFactory channelFactory = new OioDatagramChannelFactory();
    return new UdpClient(channelFactory, address, port);
  }

  public static UdpClient makeUdpClient(String address) {
    return makeUdpClient(address, DEFAULT_PORT);
  }

  public static UdpClient makeUdpClient() {
    final OioDatagramChannelFactory channelFactory = new OioDatagramChannelFactory();
    return new UdpClient(channelFactory);
  }

  public static TcpClient makeTcpClient(String address, int port) {
    checkNotNull(address, "Address cannot be null");
    checkArgument((port > 0) && (port < 65535), "Port number must be between 0 and 65535");
    final OioClientSocketChannelFactory channelFactory = new OioClientSocketChannelFactory();
    return new TcpClient(channelFactory, address, port);
  }

  public static TcpClient makeTcpClient(String address) {
    return makeTcpClient(address, DEFAULT_PORT);
  }

  public static TcpClient makeTcpClient() {
    final OioClientSocketChannelFactory channelFactory = new OioClientSocketChannelFactory();
    return new TcpClient(channelFactory);
  }

}
