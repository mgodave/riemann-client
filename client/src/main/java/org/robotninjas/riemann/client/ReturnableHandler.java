package org.robotninjas.riemann.client;

import com.aphyr.riemann.Proto;
import org.jboss.netty.channel.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.write;

class ReturnableHandler implements ChannelUpstreamHandler, ChannelDownstreamHandler {

  private final BlockingQueue<ReturnableMessage> returnables;
  private final Executor executor = Executors.newSingleThreadExecutor();

  public ReturnableHandler(BlockingQueue<ReturnableMessage> returnables) {
    this.returnables = returnables;
  }

  @Override
  public void handleDownstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      if (((MessageEvent) e).getMessage() instanceof ReturnableMessage<?>) {
        final ReturnableMessage returnable = (ReturnableMessage<?>) msgEvent.getMessage();
        // strip the returnable and send the protobuf downstream
        write(ctx, e.getFuture(), returnable.getMsg(), ((MessageEvent) e).getRemoteAddress());
        returnables.put(returnable);
        return;
      } else {
        ctx.sendDownstream(e);
      }
    } else if (e instanceof ChannelStateEvent) {
      final ChannelStateEvent stateEvent = (ChannelStateEvent) e;
      if (stateEvent.getState() == ChannelState.OPEN && (Boolean) stateEvent.getValue() == false) {
        // channel was closed
        for (ReturnableMessage returnable : returnables) {
          returnable.cancel(true);
        }
        returnables.clear();
      }
    }
    ctx.sendDownstream(e);
  }

  @Override
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      final ReturnableMessage returnable = returnables.poll();
      final Proto.Msg msg = (Proto.Msg) msgEvent.getMessage();
      returnable.handleResult(msg);
    }
    ctx.sendUpstream(e);
  }

}
