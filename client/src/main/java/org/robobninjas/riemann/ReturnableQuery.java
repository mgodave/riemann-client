package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;

import java.util.List;

class ReturnableQuery extends ReturnableMessage<List<Proto.Event>> {

  public ReturnableQuery(Proto.Msg msg) {
    super(msg);
  }

  public ReturnableQuery(Proto.Msg.Builder builder) {
    super(builder);
  }

  @Override
  public void handleResult(Proto.Msg msg) {
    if (msg.hasError()) {
      future.setException(new RiemannClientException(msg.getError()));
      return;
    }
    future.set(msg.getEventsList());
  }

}
