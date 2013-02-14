package org.robotninjas.riemann.client;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingWriteHandler extends SimpleChannelHandler {

  private final ReentrantLock writeLock = new ReentrantLock();
  private final Condition writeable = writeLock.newCondition();

  @Override
  public void channelInterestChanged(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
    try {
      writeLock.lock();
      if (e.getChannel().isWritable()) {
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
          writeable.await();
        }
      } finally {
        writeLock.unlock();
      }
    }
    super.writeRequested(ctx, e);
  }
}
