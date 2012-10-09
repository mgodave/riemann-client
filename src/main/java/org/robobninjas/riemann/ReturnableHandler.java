package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;

class ReturnableHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler {

  private static final Logger logger = LoggerFactory.getLogger(ReturnableHandler.class);

  private final ConcurrentLinkedQueue<WeakReference<ReturnableMessage<?>>> returnables =
    new ConcurrentLinkedQueue<WeakReference<ReturnableMessage<?>>>();

  @Override
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      if (((MessageEvent) e).getMessage() instanceof ReturnableMessage<?>) {
        final ReturnableMessage<?> returnable = (ReturnableMessage<?>) msgEvent.getMessage();
        returnables.offer(new WeakReference<ReturnableMessage<?>>(returnable));
        // strip the returnable and send the protobuf downstream
        Channels.write(ctx, e.getFuture(), returnable.getMsg(), ((MessageEvent) e).getRemoteAddress());
      }
    } else if (e instanceof ChannelStateEvent) {
      final ChannelStateEvent stateEvent = (ChannelStateEvent) e;
      if (stateEvent.getState() == ChannelState.OPEN && (Boolean) stateEvent.getValue() == false) {
        // channel was closed
        for (WeakReference<ReturnableMessage<?>> returnableReference : returnables) {
          final ReturnableMessage returnable = returnableReference.get();
          if (returnable != null) {
            returnable.getFuture().cancel(true);
          }
        }
        returnables.clear();
      }
      ctx.sendDownstream(e);
    }
    ctx.sendDownstream(e);
  }

  @Override
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      final WeakReference<ReturnableMessage<?>> returnableReference = returnables.poll();
      final ReturnableMessage returnable = returnableReference.get();
      if (returnable != null) {
        final Proto.Msg msg = (Proto.Msg) msgEvent.getMessage();
        returnable.handleResult(msg);
      }
    }
    ctx.sendUpstream(e);
  }

}
