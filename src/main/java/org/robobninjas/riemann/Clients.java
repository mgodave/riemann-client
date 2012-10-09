package org.robobninjas.riemann;

import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;
import org.jboss.netty.channel.socket.oio.OioDatagramChannelFactory;

public class Clients {

  public static final int DEFAULT_PORT = 5555;

  public static Client makeUdpClient(String address, int port) {
    final OioDatagramChannelFactory channelFactory = new OioDatagramChannelFactory();
    return new UdpClient(channelFactory, address, port);
  }

  public static Client makeUdpClient(String address) {
    return makeUdpClient(address, DEFAULT_PORT);
  }

  public static Client makeUdpClient() {
    final OioDatagramChannelFactory channelFactory = new OioDatagramChannelFactory();
    return new UdpClient(channelFactory);
  }

  public static Client makeTcpClient(String address, int port) {
    final OioClientSocketChannelFactory channelFactory = new OioClientSocketChannelFactory();
    return new TcpClient(channelFactory, address, port);
  }

  public static Client makeTcpClient(String address) {
    return makeTcpClient(address, DEFAULT_PORT);
  }

  public static Client makeTcpClient() {
    final OioClientSocketChannelFactory channelFactory = new OioClientSocketChannelFactory();
    return new TcpClient(channelFactory);
  }

}
