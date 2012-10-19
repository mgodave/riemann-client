package org.robobninjas.riemann;

public interface Client {

  public static final int DEFAULT_PORT = 5555;

  Connection makeConnection() throws InterruptedException;
  Connection makeConnection(String address, int port) throws InterruptedException;
  Connection makeConnection(String address) throws InterruptedException;
  void shutdown();

}
