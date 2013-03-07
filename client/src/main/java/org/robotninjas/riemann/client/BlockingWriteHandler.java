package org.robotninjas.riemann.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingWriteHandler extends SimpleChannelHandler {

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final ReentrantLock writeLock = new ReentrantLock();
  private final Condition writeable = writeLock.newCondition();

  @Override
  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    try {
      writeLock.lock();
      if (e.getChannel().isWritable()) {
        //log.debug("Writeable");
        writeable.signalAll();
      }
    } finally {
      writeLock.unlock();
    }
    super.channelInterestChanged(ctx, e);
  }

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    if (!e.getChannel().isWritable()) {
      try {
        writeLock.lock();
        while (!e.getChannel().isWritable()) {
          //log.debug("Blocked");
          writeable.await();
        }
      } finally {
        writeLock.unlock();
      }
    }
    super.writeRequested(ctx, e);
  }
}
