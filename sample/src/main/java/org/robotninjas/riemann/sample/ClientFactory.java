package org.robotninjas.riemann.sample;

import org.robobninjas.riemann.Client;

public interface ClientFactory {

  Client create(String address, int port);

}
