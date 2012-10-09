package org.robobninjas.riemann;

public interface Client {

  Connection makeConnection() throws InterruptedException;
  Connection makeConnection(String address, int port) throws InterruptedException;
  Connection makeConnection(String address) throws InterruptedException;
  void shutdown();

}
