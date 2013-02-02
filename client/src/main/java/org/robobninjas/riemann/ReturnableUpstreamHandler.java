package org.robobninjas.riemann;

import com.aphyr.riemann.Proto;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

class ReturnableUpstreamHandler implements ChannelUpstreamHandler {
  private static final Logger logger = LoggerFactory.getLogger(ReturnableDownstreamHandler.class);
  private final ConcurrentLinkedQueue<ReturnableMessage<?>> returnables;

  public ReturnableUpstreamHandler(ConcurrentLinkedQueue<ReturnableMessage<?>> returnables) {
    this.returnables = returnables;
  }
  @Override
  public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
    if (e instanceof MessageEvent) {
      final MessageEvent msgEvent = (MessageEvent) e;
      final ReturnableMessage<?> returnable = returnables.poll();
      final Proto.Msg msg = (Proto.Msg) msgEvent.getMessage();
      returnable.handleResult(msg);
    }
    ctx.sendUpstream(e);
  }
}
