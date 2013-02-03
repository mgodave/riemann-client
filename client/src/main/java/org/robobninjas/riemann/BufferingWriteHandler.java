package org.robobninjas.riemann;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BufferingWriteHandler extends SimpleChannelHandler {

  private final ReentrantLock writeLock = new ReentrantLock();
  private final Condition writeableCondition = writeLock.newCondition();

  @Override
  public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    try {
      writeLock.lock();
      while (!e.getChannel().isWritable()) {
        writeableCondition.await();
      }
    } finally {
      writeLock.unlock();
    }
    super.writeRequested(ctx, e);
  }

  @Override
  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    try {
      writeLock.lock();
      if (e.getChannel().isWritable()) {
        writeableCondition.signal();
      }
    } finally {
      writeLock.unlock();
    }
  }

}
