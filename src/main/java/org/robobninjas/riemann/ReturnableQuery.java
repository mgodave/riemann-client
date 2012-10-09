package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;

import java.util.List;

class ReturnableQuery extends ReturnableMessage<List<Proto.Event>> {

  public ReturnableQuery(Proto.Msg msg) {
    super(msg);
  }

  @Override
  public void handleResult(Proto.Msg msg) {
    if (msg.hasError()) {
      future.setException(new ReturnableException(msg.getError()));
      return;
    }
    future.set(msg.getEventsList());
  }

}
