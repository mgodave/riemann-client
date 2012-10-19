package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;

class ReturnableEvent extends ReturnableMessage<Boolean> {

  public ReturnableEvent(Proto.Msg msg) {
    super(msg);
  }

  public ReturnableEvent(Proto.Msg.Builder builder) {
    super(builder);
  }

  @Override
  public void handleResult(Proto.Msg msg) {
    if (msg.hasError()) {
      future.setException(new RiemannClientException(msg.getError()));
    } else {
      future.set(msg.getOk());
    }
  }

}
